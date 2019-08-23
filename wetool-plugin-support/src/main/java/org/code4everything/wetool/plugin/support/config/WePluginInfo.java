package org.code4everything.wetool.plugin.support.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
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
public class WePluginInfo implements BaseBean, Serializable {

    private static final long serialVersionUID = -8103599072656856290L;

    /**
     * 插件作者
     *
     * @since 1.0.0
     */
    private String author;

    /**
     * 插件名称
     *
     * @since 1.0.0
     */
    private String name;

    /**
     * 插件版本
     *
     * @since 1.0.0
     */
    private String version;

    /**
     * 实现了 {@link org.code4everything.wetool.plugin.support.WePluginSupportable} 的类
     *
     * @since 1.0.0
     */
    private String supportedClass;
}
