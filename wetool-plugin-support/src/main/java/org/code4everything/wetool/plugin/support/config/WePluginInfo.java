package org.code4everything.wetool.plugin.support.config;

import lombok.*;
import org.code4everything.boot.base.bean.BaseBean;

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
     * 实现了 {@link org.code4everything.wetool.plugin.support.WePluginSupportable} 的类全名，例如：org.code4everything.wetool.plugin.sample.WetoolSupporter
     *
     * @since 1.0.0
     */
    @NonNull
    private String supportedClass;

    public WePluginInfo(String author, String name, String version) {
        this.author = author;
        this.name = name;
        this.version = version;
    }
}
