package org.code4everything.wetool.plugin.support.cache;

import cn.hutool.cache.impl.TimedCache;
import lombok.experimental.UtilityClass;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author pantao
 * @since 2020/12/10
 */
@UtilityClass
public class Counter {

    private static final String DEFAULT_MASTER_KEY = "wetool";

    private static final WeTimedCache<WeTimedCache<Long>> TIMED_COUNTER_MAP = new WeTimedCache<>(Long.MAX_VALUE);

    public static void setMaster(String masterKey) {
        setMaster(masterKey, Long.MAX_VALUE);
    }

    public static void setMaster(String masterKey, long timeout) {
        Objects.requireNonNull(masterKey);
        WeTimedCache<Long> weTimedCache = TIMED_COUNTER_MAP.get(masterKey);
        if (Objects.isNull(weTimedCache)) {
            TimedCache<String, Long> timedCache = new TimedCache<>(Long.MAX_VALUE);
            timedCache.schedulePrune(1000);
        }
        TIMED_COUNTER_MAP.put(masterKey, weTimedCache, timeout);
    }

    public static void removeMaster(String masterKey) {
        TIMED_COUNTER_MAP.remove(masterKey);
    }

    public static Map<String, Long> getByMaster(String masterKey) {
        WeTimedCache<Long> weTimedCache = TIMED_COUNTER_MAP.get(masterKey);
        return Objects.isNull(weTimedCache) ? Collections.emptyMap() : weTimedCache.getAllKeyValues();
    }

    public static long increment(List<String> keyNodes, long addend) {
        return 0;
    }
}
