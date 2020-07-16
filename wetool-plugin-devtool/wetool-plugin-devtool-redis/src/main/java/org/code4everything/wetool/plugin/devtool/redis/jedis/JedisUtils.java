package org.code4everything.wetool.plugin.devtool.redis.jedis;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.code4everything.boot.base.ReferenceUtils;
import org.code4everything.wetool.plugin.devtool.redis.config.ConnectionConfiguration;
import redis.clients.jedis.Jedis;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author pantao
 * @since 2019/11/15
 */
@Slf4j
@UtilityClass
public class JedisUtils {

    private static final Map<String, ConnectionConfiguration> CONF_MAP = new HashMap<>(8);

    /**
     * {@link java.lang.ref.SoftReference}?
     */
    private static final Map<String, WeakReference<Jedis>> JEDIS_MAP = new HashMap<>(8);

    private static final Map<String, Integer> CURR_DB_MAP = new HashMap<>(8);

    private static RedisServer redisServer;

    private static KeyExplorer keyExplorer;

    public static void clearRedis() {
        CONF_MAP.clear();
        JEDIS_MAP.clear();
        CURR_DB_MAP.clear();
    }

    public static void putRedisConf(String alias, ConnectionConfiguration conf) {
        CONF_MAP.put(alias, conf);
        if (conf.getInitialConnect()) {
            getJedis(alias, 0);
        }
    }

    public static boolean containsServer(String alias) {
        return CONF_MAP.containsKey(alias);
    }

    public static void offerRedisServer(String alias, int db) {
        redisServer = new RedisServer(alias, db);
        getJedis(alias, db);
    }

    public static void offerKeyExplorer(RedisServer redisServer, String key, String type) {
        keyExplorer = new KeyExplorer(redisServer, key, type);
    }

    public static RedisServer getRedisServer() {
        return redisServer;
    }

    public static KeyExplorer getKeyExplorer() {
        return keyExplorer;
    }

    public static Jedis getJedis(RedisServer redisServer) {
        return getJedis(redisServer.getAlias(), redisServer.getDb());
    }

    private static Jedis getJedis(String alias, int db) {
        Jedis jedis = ReferenceUtils.unwrap(JEDIS_MAP.get(alias));
        if (Objects.isNull(jedis)) {
            // 创建一个新的连接
            ConnectionConfiguration conf = CONF_MAP.get(alias);
            jedis = new Jedis(conf.getHost(), conf.getPort());
            if (StrUtil.isNotEmpty(conf.getPassword())) {
                jedis.auth(conf.getPassword());
            }
            JEDIS_MAP.put(alias, new WeakReference<>(jedis));
        }
        // 检查连接
        if (!jedis.isConnected()) {
            jedis.connect();
        }
        // 检查DB
        int currDb = CURR_DB_MAP.getOrDefault(alias, 0);
        if (db != currDb) {
            currDb = db;
            CURR_DB_MAP.put(alias, currDb);
            jedis.select(currDb);
        }
        return jedis;
    }

    @Data
    public static class RedisServer {

        private String alias;

        private int db;

        private RedisServer(String alias, int db) {
            this.alias = alias;
            this.db = db;
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class KeyExplorer extends RedisServer {

        private String key;

        private String type;

        private KeyExplorer(RedisServer redisServer, String key, String type) {
            super(redisServer.getAlias(), redisServer.getDb());
            this.key = key;
            this.type = type;
        }
    }
}
