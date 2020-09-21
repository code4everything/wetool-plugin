package org.code4everything.wetool.plugin.devtool.redis.config;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.code4everything.boot.base.FileUtils;
import org.code4everything.boot.base.bean.BaseBean;
import org.code4everything.wetool.plugin.support.util.WeUtils;

import java.io.File;
import java.util.Collections;
import java.util.Set;

/**
 * @author pantao
 * @since 2019/11/14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class RedisConfiguration implements BaseBean {

    private static final GeoAmapConfiguration EMPTY_GEO_AMAP_CONF = new GeoAmapConfiguration();

    private static final RedisConfiguration NOTHING = new RedisConfiguration(Collections.emptySet(),
            new GeoAmapConfiguration());

    private static final String CONFIG_FILE = "conf" + File.separator + "devtool-redis-config.json";

    private Set<ConnectionConfiguration> servers;

    private GeoAmapConfiguration geoAmapConf;

    public static RedisConfiguration getConfiguration() {
        String path = WeUtils.parsePathByOs(CONFIG_FILE);
        if (StrUtil.isEmpty(path)) {
            return NOTHING;
        }
        String json = FileUtil.readUtf8String(path);
        try {
            return JSON.parseObject(json, RedisConfiguration.class);
        } catch (Exception e) {
            return NOTHING;
        }
    }

    public static String getPath() {
        return StrUtil.emptyToDefault(WeUtils.parsePathByOs(CONFIG_FILE), FileUtils.currentWorkDir(CONFIG_FILE));
    }

    public GeoAmapConfiguration getGeoAmapConf() {
        return ObjectUtil.defaultIfNull(geoAmapConf, EMPTY_GEO_AMAP_CONF);
    }
}
