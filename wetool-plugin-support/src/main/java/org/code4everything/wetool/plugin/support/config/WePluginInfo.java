package org.code4everything.wetool.plugin.support.config;

import cn.hutool.core.util.StrUtil;
import lombok.*;
import org.code4everything.boot.base.bean.BaseBean;
import org.code4everything.wetool.plugin.support.WePluginSupporter;

import java.io.Serializable;

/**
 * @author pantao
 * @since 2019/8/23
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"supportedClass", "requireWetoolVersion"})
public class WePluginInfo implements BaseBean, Serializable {

    private static final long serialVersionUID = -8103599072656856290L;

    private static final String DEFAULT_REQUIRE_WETOOL_VERSION = "1.0.0";

    /**
     * 插件作者
     *
     * @since 1.0.0
     */
    @NonNull
    private String author;

    /**
     * 插件名称
     *
     * @since 1.0.0
     */
    @NonNull
    private String name;

    /**
     * 插件版本
     *
     * @since 1.0.0
     */
    @NonNull
    private String version;

    /**
     * 要求Wetool-Plugin-Support的最低版本号（即依赖的wetool-plugin-support版本号），例如：1.0.0
     *
     * @since 1.0.0
     */
    @NonNull
    private String requireWetoolVersion;

    /**
     * 实现了 {@link WePluginSupporter} 的类全名，例如：org.code4everything.wetool.plugin.sample.WetoolSupporter
     *
     * @since 1.0.0
     */
    @NonNull
    private String supportedClass;

    public WePluginInfo(String author, String name, String version) {
        this(author, name, version, DEFAULT_REQUIRE_WETOOL_VERSION);
    }

    public WePluginInfo(String author, String name, String version, String requireWetoolVersion) {
        this.author = author;
        this.name = name;
        this.version = version;
        this.requireWetoolVersion = requireWetoolVersion;
    }

    @Generated
    public String getRequireWetoolVersion() {
        return StrUtil.emptyToDefault(requireWetoolVersion, DEFAULT_REQUIRE_WETOOL_VERSION);
    }
}
