package org.code4everything.wetool.plugin.support.config;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import lombok.*;
import org.code4everything.boot.base.bean.BaseBean;
import org.code4everything.wetool.plugin.support.WePluginSupporter;
import org.code4everything.wetool.plugin.support.constant.AppConsts;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author pantao
 * @since 2019/8/23
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class WePluginInfo implements BaseBean, Serializable {

    private static final long serialVersionUID = -8103599072656856290L;

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

    /**
     * 是否隔离插件，即使用独立的类加载器
     *
     * @since 1.1.1
     */
    private Boolean isolated;

    /**
     * 支持的操作系统：windows, mac, linux
     */
    private String supportOs;

    public WePluginInfo(String author, String name, String version) {
        this(author, name, version, AppConsts.CURRENT_VERSION);
    }

    public WePluginInfo(String author, String name, String version, String requireWetoolVersion) {
        this.author = author;
        this.name = name;
        this.version = version;
        this.requireWetoolVersion = requireWetoolVersion;
    }

    @Generated
    public boolean getIsolated() {
        return BooleanUtil.isTrue(isolated);
    }

    @Generated
    public String getRequireWetoolVersion() {
        return StrUtil.emptyToDefault(requireWetoolVersion, AppConsts.CURRENT_VERSION);
    }

    public String getSupportOs() {
        return StrUtil.emptyToDefault(supportOs, "windows,linux,mac");
    }

    @Generated


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        WePluginInfo that = (WePluginInfo) o;
        return Objects.equals(getAuthor(), that.getAuthor()) && Objects.equals(getName(), that.getName()) && Objects.equals(getVersion(), that.getVersion());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAuthor(), getName(), getVersion());
    }
}
