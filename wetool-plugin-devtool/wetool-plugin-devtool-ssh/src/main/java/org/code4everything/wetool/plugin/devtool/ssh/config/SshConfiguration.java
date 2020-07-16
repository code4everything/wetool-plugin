package org.code4everything.wetool.plugin.devtool.ssh.config;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.CharsetUtil;
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
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Set;

/**
 * @author pantao
 * @since 2019/11/25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class SshConfiguration implements BaseBean {

    private static final SshConfiguration NOTHING = new SshConfiguration(null, null, Collections.emptySet());

    private static final String CONFIG_FILE = "conf" + File.separator + "devtool-ssh-config.json";

    private String defaultConsolePath;

    private String localCharset;

    private Set<ServerConfiguration> servers;

    public static SshConfiguration getConfiguration() {
        String path = WeUtils.parsePathByOs(CONFIG_FILE);
        if (StrUtil.isEmpty(path)) {
            return NOTHING;
        }
        String json = FileUtil.readUtf8String(path);
        try {
            return JSON.parseObject(json, SshConfiguration.class);
        } catch (Exception e) {
            return NOTHING;
        }
    }

    public static String getPath() {
        return StrUtil.emptyToDefault(WeUtils.parsePathByOs(CONFIG_FILE), FileUtils.currentWorkDir(CONFIG_FILE));
    }

    public Charset getLocalCharset() {
        return CharsetUtil.charset(localCharset);
    }

    public String getDefaultConsolePath() {
        return StrUtil.emptyToDefault(defaultConsolePath, FileUtil.getUserHomePath());
    }

    public Set<ServerConfiguration> getServers() {
        return servers;
    }
}
