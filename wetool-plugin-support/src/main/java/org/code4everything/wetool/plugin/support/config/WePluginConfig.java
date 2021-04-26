package org.code4everything.wetool.plugin.support.config;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.Generated;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author pantao
 * @since 2021/4/26
 */
@Data
public class WePluginConfig {

    private Map<String, Boolean> initBoot;

    @JSONField(serialize = false, deserialize = false)
    private transient boolean changed = false;

    public boolean putInitBootIfNotExists(WePluginInfo info, boolean initBoot) {
        String key = info.getAuthor() + "-" + info.getName();
        Map<String, Boolean> map = getInitBoot();
        Boolean boot = map.get(key);
        if (Objects.isNull(boot)) {
            map.put(key, initBoot);
            setChanged(true);
        }
        return ObjectUtil.defaultIfNull(boot, initBoot);
    }

    @Generated
    public Map<String, Boolean> getInitBoot() {
        if (Objects.isNull(initBoot)) {
            setInitBoot(new HashMap<>());
        }
        return initBoot;
    }

    @Generated
    private void setChanged(boolean changed) {
        this.changed = changed;
    }
}
