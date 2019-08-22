package org.code4everything.wetool.plugin.support;

import org.code4everything.boot.base.bean.BaseBean;

/**
 * @author pantao
 * @since 2019/8/22
 */
public interface WePluginInfo extends BaseBean {

    /**
     * 获取作者
     *
     * @return 作者
     *
     * @since 1.5.0
     */
    String getAuthor();

    /**
     * 获取插件名
     *
     * @return 插件名
     *
     * @since 1.5.0
     */
    String getName();

    /**
     * 获取插件版本
     *
     * @return 插件版本
     *
     * @since 1.5.0
     */
    String getVersion();
}
