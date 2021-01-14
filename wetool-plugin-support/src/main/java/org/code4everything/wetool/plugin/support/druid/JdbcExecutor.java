package org.code4everything.wetool.plugin.support.druid;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.lang.Holder;
import cn.hutool.core.lang.func.Func1;
import cn.hutool.core.lang.func.VoidFunc1;
import cn.hutool.core.map.TolerantMap;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * @author pantao
 * @since 2020/11/10
 */
@Slf4j
@RequiredArgsConstructor
public class JdbcExecutor {

    private static final Map<String, JdbcExecutor> MAP = new HashMap<>(4);

    private final Pattern existsSqlPatten = Pattern.compile("^\\s*?select count(.*)>0 from.*");

    private final DruidDataSource dataSource;

    public static JdbcExecutor getJdbcExecutor(String name) {
        return getJdbcExecutor(DruidSource.getDruidDataSource(name));
    }

    public static JdbcExecutor getJdbcExecutor(DruidDataSource dataSource) {
        Objects.requireNonNull(dataSource);
        String name = dataSource.getName();
        return MAP.computeIfAbsent(name, s -> new JdbcExecutor(dataSource));
    }

    public boolean exists(String sql, @Nullable List<Object> params) {
        if (!existsSqlPatten.matcher(sql).matches()) {
            throw new SqlException("sql syntax error for exist query, must like 'select count(*)>0 from...'");
        }
        return ObjectUtil.defaultIfNull(selectOne(sql, params, Boolean.class), false);
    }

    public <T> T selectOne(String sql, @Nullable List<Object> params, Class<T> clz) {
        Holder<T> holder = new Holder<>(null);
        query(sql, params, resultSet -> {
            if (resultSet.next()) {
                holder.set(mapClass(resultSet, clz, null));
            }
        }, null);
        return holder.get();
    }

    public long count(String sql, @Nullable List<Object> params) {
        Holder<Long> holder = new Holder<>(0L);
        query(sql, params, resultSet -> {
            if (resultSet.next()) {
                holder.set(JdbcOpsUtils.fastCast(resultSet.getLong(1), Long.class));
            }
        }, null);
        return ObjectUtil.defaultIfNull(holder.get(), 0L);
    }

    /**
     * Map映射查询
     *
     * @param keyGetter Map Key回调
     */
    public <K, V> Map<K, List<V>> selectMapList(String sql, @Nullable List<Object> params, Class<V> clz,
                                                Func1<V, K> keyGetter) {
        Holder<Map<K, List<V>>> holder = new Holder<>();
        // 查询SQL并处理结果
        query(sql, params, resultSet -> holder.set(new HashMap<>(resultSet.getFetchSize(), 1)), resultSet -> {
            V v = mapClass(resultSet, clz, null);
            K k = keyGetter.call(v);
            List<V> list = holder.get().get(k);
            if (Objects.isNull(list)) {
                list = new ArrayList<>();
                holder.get().put(k, list);
            }
            list.add(v);
        });
        return holder.get();
    }

    /**
     * 基本类型Map
     */
    public <K, V> Map<K, V> selectMap(String sql, @Nullable List<Object> params, Class<K> keyType, Class<V> valueType) {
        Holder<Map<K, V>> holder = new Holder<>();
        // 查询SQL并处理结果
        query(sql, params, resultSet -> holder.set(new HashMap<>(resultSet.getFetchSize(), 1)),
                resultSet -> holder.get().put(JdbcOpsUtils.fastCast(resultSet.getObject(1), keyType),
                        JdbcOpsUtils.fastCast(resultSet.getObject(2), valueType)));
        return holder.get();
    }

    /**
     * Map映射查询
     *
     * @param keyGetter Map Key回调
     */
    public <K, V> Map<K, V> selectMap(String sql, @Nullable List<Object> params, Class<V> clz, Func1<V, K> keyGetter) {
        Holder<Map<K, V>> holder = new Holder<>();
        // 查询SQL并处理结果
        query(sql, params, resultSet -> holder.set(new HashMap<>(resultSet.getFetchSize(), 1)), resultSet -> {
            V v = mapClass(resultSet, clz, null);
            holder.get().put(keyGetter.call(v), v);
        });
        return holder.get();
    }

    /**
     * Map映射查询
     *
     * @param keyGetter Map Key回调
     * @param valueGetter Map Value回调
     */
    public <K, V> Map<K, V> selectMap(String sql, @Nullable List<Object> params, Func1<ResultSet, K> keyGetter,
                                      Func1<ResultSet, V> valueGetter) {
        Holder<Map<K, V>> holder = new Holder<>();
        // 查询SQL并处理结果
        query(sql, params, resultSet -> holder.set(new HashMap<>(resultSet.getFetchSize(), 1)),
                resultSet -> holder.get().put(keyGetter.call(resultSet), valueGetter.call(resultSet)));
        return holder.get();
    }

    public <T> List<T> selectList(String sql, @Nullable List<Object> params, Class<T> clz) {
        return selectList(sql, params, clz, null);
    }

    public <T> Set<T> selectSet(String sql, @Nullable List<Object> params, Class<T> clz) {
        return selectSet(sql, params, clz, null);
    }

    /**
     * 查询
     *
     * @param mapped 对象映射完成后的回调
     */
    public <T> Set<T> selectSet(String sql, @Nullable List<Object> params, Class<T> clz,
                                @Nullable VoidFunc1<T> mapped) {
        Holder<Set<T>> holder = new Holder<>();
        // 查询SQL并处理结果
        query(sql, params, resultSet -> holder.set(new HashSet<>(resultSet.getFetchSize(), 1)),
                resultSet -> holder.get().add(mapClass(resultSet, clz, mapped)));
        return holder.get();
    }

    /**
     * 查询
     *
     * @param predicate 过滤器
     */
    public <T> List<T> selectFilteredList(String sql, @Nullable List<Object> params, Class<T> clz,
                                          Predicate<T> predicate) {
        Holder<List<T>> holder = new Holder<>();
        // 查询SQL并处理结果
        query(sql, params, resultSet -> holder.set(new ArrayList<>(resultSet.getFetchSize())), resultSet -> {
            final T t = mapClass(resultSet, clz, null);
            if (predicate.test(t)) {
                holder.get().add(t);
            }
        });
        return holder.get();
    }

    /**
     * 查询
     *
     * @param mapped 对象映射完成后的回调
     */
    public <T> List<T> selectList(String sql, @Nullable List<Object> params, Class<T> clz,
                                  @Nullable VoidFunc1<T> mapped) {
        Holder<List<T>> holder = new Holder<>();
        // 查询SQL并处理结果
        query(sql, params, resultSet -> holder.set(new ArrayList<>(resultSet.getFetchSize())),
                resultSet -> holder.get().add(mapClass(resultSet, clz, mapped)));
        return holder.get();
    }

    public JSONArray selectJsonArray(String sql, @Nullable List<Object> params) {
        JSONArray array = new JSONArray();
        query(sql, params, null, resultSet -> {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            JSONObject jsonObject = new JSONObject();

            for (int i = 1; i <= columnCount; i++) {
                jsonObject.put(metaData.getColumnName(i), resultSet.getObject(i));
            }

            array.add(jsonObject);
        });
        return array;
    }

    public List<Map<String, Object>> select(String sql, @Nullable List<Object> params) {
        List<Map<String, Object>> list = new ArrayList<>();
        query(sql, params, null, resultSet -> {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            Map<String, Object> map = new LinkedHashMap<>(columnCount, 1);

            for (int i = 1; i <= columnCount; i++) {
                map.put(metaData.getColumnName(i), resultSet.getObject(i));
            }

            list.add(map);
        });
        return list;
    }

    /**
     * 查询
     *
     * @param sql SQL
     * @param completed 查询完成后的回调
     * @param iter ResultSet遍历回调
     */
    public void query(String sql, @Nullable VoidFunc1<ResultSet> completed, @Nullable VoidFunc1<ResultSet> iter) {
        executeWithConnCall((connection, sqlHolder) -> {
            sqlHolder.set(sql);
            try (Statement statement = connection.createStatement(); ResultSet resultSet =
                    statement.executeQuery(sql)) {
                handleResult(resultSet, completed, iter);
            }
        });
    }

    /**
     * 查询
     *
     * @param sql SQL
     * @param completed 查询完成后的回调
     * @param iter ResultSet遍历回调
     */
    public void query(String sql, @Nullable List<Object> params, @Nullable VoidFunc1<ResultSet> completed,
                      @Nullable VoidFunc1<ResultSet> iter) {
        if (CollUtil.isEmpty(params)) {
            query(sql, completed, iter);
            return;
        }
        executeWithPreparedStatementCall(sql, params, (statement, sqlHolder) -> {
            sqlHolder.set(statement.toString().replaceFirst(".+?select ", "select "));
            try (ResultSet resultSet = statement.executeQuery()) {
                handleResult(resultSet, completed, iter);
            }
        });
    }

    /**
     * 更新数据
     *
     * @param sql SQL
     */
    public int update(String sql, List<Object> params) {
        Holder<Integer> holder = new Holder<>();
        executeWithPreparedStatementCall(sql, params, (statement, sqlHolder) -> {
            // 去掉预编译SQL的冗余前缀，方便打印
            String newSql = statement.toString().replaceFirst(".+?insert ", "insert ");
            newSql = newSql.replaceFirst(".+?delete ", "delete ");
            sqlHolder.set(newSql.replaceFirst(".+?update ", "update "));
            holder.set(statement.executeUpdate());
        });
        return ObjectUtil.defaultIfNull(holder.get(), 0);
    }

    private <T> T mapClass(ResultSet resultSet, Class<T> clz, VoidFunc1<T> mapped) throws Exception {
        return JdbcOpsUtils.mapClass(resultSet, clz, mapped);
    }

    /**
     * 获取数据库连接，并回调执行
     */
    private void executeWithConnCall(ExecutorCaller<Connection> caller) {
        long start = System.currentTimeMillis();
        DruidPooledConnection connection;
        try {
            connection = dataSource.getConnection();
        } catch (SQLException e) {
            log.error("failed to get connection from datasource: " + ExceptionUtil.stacktraceToString(e));
            return;
        }

        Holder<String> sqlHolder = new Holder<>("");

        JdbcOpsUtils.getLocalUnparsed().set(new TolerantMap<>(16, false));
        JdbcOpsUtils.getParserChain().initParse();
        try {
            caller.executeAndSetSql(connection, sqlHolder);
            log.debug("[{}ms] execute sql [{}]", System.currentTimeMillis() - start, sqlHolder.get());
        } catch (Exception e) {
            log.error("execute sql failed [{}] \r\n{}", sqlHolder.get(), ExceptionUtil.stacktraceToString(e));
        } finally {
            // 释放连接
            try {
                connection.recycle();
            } catch (SQLException e) {
                log.error("release connection error: {}", ExceptionUtil.stacktraceToString(e, Integer.MAX_VALUE));
            }
            JdbcOpsUtils.getLocalUnparsed().remove();
            JdbcOpsUtils.getParserChain().destroyMap();
        }
    }

    @SuppressWarnings("rawtypes")
    private void executeWithPreparedStatementCall(String sql, List<Object> params,
                                                  ExecutorCaller<PreparedStatement> caller) {
        executeWithConnCall((connection, sqlHolder) -> {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                if (CollUtil.isNotEmpty(params)) {
                    // 填充参数
                    for (int i = 0; i < params.size(); i++) {
                        Object obj = params.get(i);
                        if (Objects.isNull(obj)) {
                            statement.setNull(i + 1, Types.JAVA_OBJECT);
                        } else {
                            if (EnumUtil.isEnum(obj)) {
                                //noinspection SingleStatementInBlock
                                obj = ((Enum) obj).name();
                            }
                            statement.setObject(i + 1, obj);
                        }
                    }
                }
                caller.executeAndSetSql(statement, sqlHolder);
            }
        });
    }

    private void handleResult(ResultSet resultSet, @Nullable VoidFunc1<ResultSet> completed,
                              @Nullable VoidFunc1<ResultSet> iter) throws Exception {
        if (ObjectUtil.isNotNull(completed)) {
            completed.call(resultSet);
        }
        JdbcOpsUtils.handleResult(resultSet, iter);
    }
}
