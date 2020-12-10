package org.code4everything.wetool.plugin.support.cache;

import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.collection.IterUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.experimental.UtilityClass;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 计数器缓存
 *
 * @author pantao
 * @since 2020/12/10
 */
@UtilityClass
public class Counter {

    private static final String DEFAULT_MASTER_KEY = "wetool";

    private static final WeTimedCache<WeTimedCache<Long>> TIMED_COUNTER_MAP = new WeTimedCache<>(Long.MAX_VALUE);

    private static final WeTimedCache<Long> EMPTY_CACHE = new WeTimedCache<>(1);

    public static WeTimedCache<Long> setMaster(String masterKey) {
        return setMaster(masterKey, Long.MAX_VALUE);
    }

    public static WeTimedCache<Long> setMaster(String masterKey, long timeout) {
        Objects.requireNonNull(masterKey);

        if (TIMED_COUNTER_MAP.isEmpty()) {
            synchronized (Counter.class) {
                if (TIMED_COUNTER_MAP.isEmpty()) {
                    TIMED_COUNTER_MAP.schedulePrune(1000);
                }
            }
        }

        WeTimedCache<Long> weTimedCache = TIMED_COUNTER_MAP.get(masterKey);
        if (Objects.isNull(weTimedCache)) {
            TimedCache<String, Long> timedCache = new TimedCache<>(Long.MAX_VALUE);
            timedCache.schedulePrune(1000);
        }
        TIMED_COUNTER_MAP.put(masterKey, weTimedCache, timeout);
        return weTimedCache;
    }

    public static void removeMaster(String masterKey) {
        TIMED_COUNTER_MAP.remove(masterKey);
    }

    public static Map<String, Long> getByDefaultMaster() {
        return getByMaster(DEFAULT_MASTER_KEY);
    }

    public static Map<String, Long> getByMaster(String masterKey) {
        WeTimedCache<Long> weTimedCache = TIMED_COUNTER_MAP.get(masterKey);
        return Objects.isNull(weTimedCache) ? Collections.emptyMap() : weTimedCache.getAllKeyValues();
    }

    public static boolean existsMaster(String masterKey) {
        return TIMED_COUNTER_MAP.containsKey(masterKey);
    }

    public static long increment(String secondKey, long addend) {
        return increment(null, secondKey, addend, Long.MAX_VALUE);
    }

    public static void incrementFromQuery(List<Map<String, Object>> list, String masterKey, String nameKey,
                                          String valueKey) {
        incrementFromQuery(list, masterKey, nameKey, valueKey, Long.MAX_VALUE);
    }

    public static void incrementFromQuery(List<Map<String, Object>> list, String masterKey, String nameKey,
                                          String valueKey, long timeout) {
        Map<String, Number> kvMap = IterUtil.toMap(list, map -> ObjectUtil.toString(map.get(nameKey)), map -> {
            Object value = map.get(valueKey);
            return Objects.nonNull(value) && value instanceof Number ? (Number) value : 0;
        });
        incrementFromQuery(kvMap, masterKey, timeout);
    }

    public static void incrementFromQuery(Map<String, ? extends Number> map, String masterKey) {
        incrementFromQuery(map, masterKey, Long.MAX_VALUE);
    }

    public static void incrementFromQuery(Map<String, ? extends Number> map, String masterKey, long timeout) {
        if (MapUtil.isEmpty(map)) {
            return;
        }
        map.forEach((k, v) -> increment(masterKey, k, v.longValue(), timeout));
    }

    public static long increment(String masterKey, String secondKey, long addend, long timeout) {
        masterKey = StrUtil.blankToDefault(masterKey, DEFAULT_MASTER_KEY);
        WeTimedCache<Long> weTimedCache = TIMED_COUNTER_MAP.get(masterKey);
        if (Objects.isNull(weTimedCache)) {
            weTimedCache = setMaster(masterKey, Long.MAX_VALUE);
        }

        long value = ObjectUtil.defaultIfNull(weTimedCache.get(secondKey), 0L) + addend;
        weTimedCache.put(secondKey, value, timeout);
        return value;
    }

    public static long removeCounter(String secondKey) {
        return removeCounter(null, secondKey);
    }

    public static long removeCounter(String masterKey, String secondKey) {
        long value = getCounter(masterKey, secondKey);
        // 设置立即过期
        increment(masterKey, secondKey, 0L, 1);
        return value;
    }

    public static long getCounter(String secondKey) {
        return getCounter(null, secondKey);
    }

    public static long getCounter(String masterKey, String secondKey) {
        masterKey = StrUtil.blankToDefault(masterKey, DEFAULT_MASTER_KEY);
        WeTimedCache<Long> weTimedCache = TIMED_COUNTER_MAP.get(masterKey);
        if (Objects.isNull(weTimedCache)) {
            return 0;
        }
        Long value = weTimedCache.get(secondKey);
        return ObjectUtil.defaultIfNull(value, 0L);
    }

    public static boolean existsCounter(String secondKey) {
        return existsCounter(StrUtil.blankToDefault(DEFAULT_MASTER_KEY, secondKey));
    }

    public static boolean existsCounter(String masterKey, String secondKey) {
        return ObjectUtil.defaultIfNull(TIMED_COUNTER_MAP.get(masterKey), EMPTY_CACHE).containsKey(secondKey);
    }
}
