package org.code4everything.wetool.plugin.support.druid;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.core.lang.func.VoidFunc1;
import cn.hutool.core.map.TolerantMap;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.util.TypeUtils;
import com.google.common.base.CaseFormat;
import lombok.experimental.UtilityClass;

import java.beans.Transient;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * @author pantao
 * @since 2020/11/10
 */
@UtilityClass
public class JdbcOpsUtils {

    /**
     * 方法名缓存
     */
    private static final Map<Class<?>, List<Method>> METHOD_CACHE = new ConcurrentHashMap<>(64);

    /**
     * 数据库字段缓存
     */
    private static final Map<Method, String> FIELD_CACHE = new ConcurrentHashMap<>(256);

    private static final Set<Class<?>> BASIC_TYPE_CACHE = new ConcurrentHashSet<>(16);

    /**
     * 异常字段缓存
     */
    private static final ThreadLocal<Map<String, Boolean>> LOCAL_UNPARSED = new ThreadLocal<>();

    private static final Pattern SQL_CONTAINS_WHERE_PATTERN = Pattern.compile("^[a-z].*from.*where.*[a-z]$");

    private static final String VERSION = "_version";

    private static ParserChain parserChain;

    static {
        // JSON字段解析
        parserChain = new ParserChain() {
            @Override
            public boolean parse(ResultSet resultSet, Object obj, Method method, String name) {
                if (shouldNonParse(name)) {
                    // 放弃这个字段的JSON解析
                    return false;
                }
                Field field = ReflectUtil.getField(obj.getClass(), getLowerCamel(method));
                JsonField jsonField = field.getAnnotation(JsonField.class);
                if (Objects.isNull(jsonField)) {
                    return unParse(name);
                }
                final Class<?> type = field.getType();
                try {
                    String json = resultSet.getString(name);
                    Object value;
                    if (jsonField.isList()) {
                        value = JSON.parseArray(json, jsonField.listType());
                    } else if (jsonField.isCustom()) {
                        value = json;
                        if (!JsonField.DEFAULT_METHOD_NAME.equals(jsonField.methodName())) {
                            method = ReflectUtil.getMethod(obj.getClass(), jsonField.methodName(), String.class);
                        }
                    } else if (jsonField.isObject()) {
                        boolean noneType = None.class == jsonField.objectType();
                        value = JSON.parseObject(json, noneType ? type : jsonField.objectType());
                    } else {
                        value = JSON.parseObject(json, type);
                    }
                    if (ObjectUtil.isNotNull(value)) {
                        ReflectUtil.invoke(obj, method, value);
                    }
                } catch (Exception e) {
                    return unParse(name);
                }
                return true;
            }
        };
        // 枚举字段解析
        parserChain.next = new ParserChain() {
            @Override
            public boolean parse(ResultSet resultSet, Object obj, Method method, String name) {
                if (shouldNonParse(name)) {
                    // 放弃这个字段的枚举解析
                    return false;
                }
                @SuppressWarnings("rawtypes") Class type = ReflectUtil.getField(obj.getClass(),
                        getLowerCamel(method)).getType();
                if (type.isEnum()) {
                    try {
                        String value = resultSet.getString(name);
                        if (ObjectUtil.isNotNull(value)) {
                            //noinspection unchecked
                            ReflectUtil.invoke(obj, method, Enum.valueOf(type, value));
                        }
                    } catch (Exception e) {
                        // 忽略这个字段
                        return unParse(name);
                    }
                    return true;
                }
                return unParse(name);
            }
        };
        // 普通字段解析
        parserChain.next.next = new ParserChain() {
            @Override
            public boolean parse(ResultSet resultSet, Object obj, Method method, String name) {
                try {
                    Object value = resultSet.getObject(name);
                    if (ObjectUtil.isNotNull(value)) {
                        ReflectUtil.invoke(obj, method, fastCast(value, method.getParameterTypes()[0]));
                    }
                } catch (Exception e) {
                    // 忽略这个字段
                    LOCAL_UNPARSED.get().put(name, true);
                }
                return true;
            }
        };
    }

    static ThreadLocal<Map<String, Boolean>> getLocalUnparsed() {
        return LOCAL_UNPARSED;
    }

    static ParserChain getParserChain() {
        return parserChain;
    }

    public static String handleWhere(String where, boolean deleted) {
        where = StrUtil.trim(where);
        if (StrUtil.isEmpty(where)) {
            return deleted ? " where is_deleted=false" : "";
        }
        where = " " + StrUtil.addPrefixIfNot(where.toLowerCase(), "where ");
        if (deleted) {
            where += " and is_deleted=false";
        }
        return where;
    }

    public static String dateBetween(String dateField, @Nullable Date begin, @Nullable Date end) {
        String sql = "";
        if (Objects.nonNull(begin)) {
            sql = StrUtil.format("unix_timestamp({})>={}", dateField, begin.getTime() / 1000);
        }
        if (Objects.nonNull(end)) {
            String sep = Objects.isNull(begin) ? "" : " and ";
            sql += sep + StrUtil.format("unix_timestamp({})<={}", dateField, end.getTime() / 1000);
        }
        return sql;
    }

    /**
     * 处理结果集
     *
     * @param resultSet 结果集
     * @param iter 解析行
     *
     * @throws Exception 如果发生异常则抛出
     */
    public static void handleResult(ResultSet resultSet, @Nullable VoidFunc1<ResultSet> iter) throws Exception {
        if (Objects.isNull(iter)) {
        }
        while (resultSet.next()) {
            iter.call(resultSet);
        }
    }

    public static String buildIn(String sql, String fieldName, Collection<?> items, boolean ignoreEmpty) {
        if (ignoreEmpty && CollUtil.isEmpty(items)) {
            return sql;
        }

        sql = StrUtil.trim(sql.toLowerCase());
        sql += (SQL_CONTAINS_WHERE_PATTERN.matcher(sql).matches() ? " and " : " where ") + fieldName;

        if (CollUtil.isNotEmpty(items) && items.size() == 1) {
            Object item = items.stream().findFirst().get();
            String str = StrUtil.format("'{}'", item.toString());
            return sql + "=" + (item instanceof CharSequence ? str : item.toString());
        }
        return sql + " in " + toInStr(items);
    }

    public static String toInStr(Collection<?> items) {
        return toInStr(items, "null");
    }

    public static String toInStr(Collection<?> items, String defaultIfEmpty) {
        if (CollUtil.isEmpty(items)) {
            return "(" + defaultIfEmpty + ")";
        }
        StringBuilder sb = new StringBuilder().append("(");
        String seq = "";
        for (Object item : items) {
            sb.append(seq);
            if (item instanceof CharSequence) {
                sb.append("'").append(item).append("'");
            } else {
                sb.append(item);
            }
            seq = ",";
        }
        return sb.append(")").toString();
    }

    public static <T> T mapClass(ResultSet resultSet, Class<T> clz, VoidFunc1<T> mapped) throws Exception {
        T t;
        if (basicTypeParse(clz)) {
            t = fastCast(resultSet.getObject(1), clz);
        } else {
            t = ReflectUtil.newInstance(clz);
            List<Method> methods = listSetMethods(clz);
            for (Method method : methods) {
                // 空的字段名表示发生过异常，直接忽略
                String name = getFieldName(method);
                if (LOCAL_UNPARSED.get().get(name)) {
                    continue;
                }
                Transient ignore = method.getAnnotation(Transient.class);
                if (Objects.nonNull(ignore)) {
                    LOCAL_UNPARSED.get().put(name, true);
                    continue;
                }
                parserChain.doParse(resultSet, t, method, name);

                // 当字段解析失败时，判断是否是boolean类型，并兼容子
                if (LOCAL_UNPARSED.get().get(name)) {
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    if (parameterTypes.length == 1 && Boolean.class.isAssignableFrom(parameterTypes[0])) {
                        name = "is_" + name;
                        parserChain.doParse(resultSet, t, method, name);
                        // 不管解析是否，都更新方法对应的数据库字段名
                        FIELD_CACHE.put(method, name);
                    }
                }
            }

            // 映射version字段
            if (!LOCAL_UNPARSED.get().get(VERSION)) {
                try {
                    ReflectUtil.setFieldValue(t, "version", resultSet.getLong(VERSION));
                } catch (Exception e) {
                    LOCAL_UNPARSED.get().put(VERSION, true);
                }
            }
        }
        if (ObjectUtil.isNotNull(mapped)) {
            mapped.call(t);
        }
        return t;
    }

    public static <T> T fastCast(Object value, Class<T> paramType) {
        return TypeUtils.castToJavaBean(value, paramType);
    }

    /**
     * 缓存类的set方法
     */
    private static List<Method> listSetMethods(Class<?> clz) {
        List<Method> methods = METHOD_CACHE.get(clz);
        if (Objects.isNull(methods)) {
            methods = Arrays.asList(ReflectUtil.getMethods(clz, method -> method.getName().startsWith("set")));
            // 如果没有set方法，设置一个空list
            if (CollUtil.isEmpty(methods)) {
                methods = Collections.emptyList();
            }
            METHOD_CACHE.put(clz, methods);
        }
        return methods;
    }

    /**
     * 解析数据库字段名
     */
    private static String getFieldName(Method method) {
        String name = FIELD_CACHE.get(method);
        if (ObjectUtil.isNull(name)) {
            String test = StrUtil.removePrefix(method.getName(), "set");
            name = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, test);
            FIELD_CACHE.put(method, name);
        }
        return name;
    }

    private static boolean basicTypeParse(Class<?> clz) {
        if (METHOD_CACHE.containsKey(clz)) {
            return false;
        }
        if (BASIC_TYPE_CACHE.contains(clz)) {
            return true;
        }
        boolean isBasic = ClassUtil.isBasicType(clz) || CharSequence.class.isAssignableFrom(clz);
        isBasic = isBasic || Date.class.isAssignableFrom(clz) || clz.isEnum();
        if (isBasic) {
            BASIC_TYPE_CACHE.add(clz);
            return true;
        }
        return false;
    }

    static class ParserChain {

        /**
         * 字段名缓存
         */
        private Map<Method, String> lowerCamelCache = null;

        private ParserChain next;

        private ThreadLocal<Map<String, Boolean>> unparsedMap = new ThreadLocal<>();

        void initParse() {
            unparsedMap.set(new TolerantMap<>(16, false));
            if (Objects.nonNull(next)) {
                next.initParse();
            }
        }

        void destroyMap() {
            unparsedMap.remove();
            if (Objects.nonNull(next)) {
                next.destroyMap();
            }
        }

        /**
         * 解析字段名
         */
        public String getLowerCamel(Method method) {
            if (Objects.isNull(lowerCamelCache)) {
                lowerCamelCache = new ConcurrentHashMap<>(64);
            }
            String name = lowerCamelCache.get(method);
            if (ObjectUtil.isNull(name)) {
                String test = StrUtil.removePrefix(method.getName(), "set");
                name = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, test);
                lowerCamelCache.put(method, name);
            }
            return name;
        }

        boolean shouldNonParse(String name) {
            return this.unparsedMap.get().get(name);
        }

        boolean unParse(String name) {
            this.unparsedMap.get().put(name, true);
            return false;
        }

        public boolean parse(ResultSet resultSet, Object obj, Method method, String name) {
            return false;
        }

        private void doParse(ResultSet resultSet, Object obj, Method method, String name) {
            if (parse(resultSet, obj, method, name) || Objects.isNull(next)) {
                return;
            }
            next.doParse(resultSet, obj, method, name);
        }
    }
}

