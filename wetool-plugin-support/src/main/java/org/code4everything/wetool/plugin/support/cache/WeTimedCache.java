package org.code4everything.wetool.plugin.support.cache;

import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.lang.func.Func0;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author pantao
 * @since 2020/12/10
 */
public class WeTimedCache<K> extends TimedCache<String, K> {

    public WeTimedCache(long timeout) {
        super(timeout, new ConcurrentHashMap<>(8));
    }

    @Override
    public K get(String key) {
        return get(key, false);
    }

    @Override
    public K get(String key, Func0<K> supplier) {
        return get(key, false, supplier);
    }

    public Map<String, K> getAllKeyValues() {
        Map<String, K> map = new HashMap<>(cacheMap.size(), 1);
        cacheMap.forEach((k, v) -> map.put(k, v.getValue()));
        return map;
    }
}
