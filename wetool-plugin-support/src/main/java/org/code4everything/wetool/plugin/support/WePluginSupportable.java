package org.code4everything.wetool.plugin.support;

import javafx.scene.control.MenuItem;

/**
 * 工具插件接口
 *
 * @author pantao
 * @since 2019/8/22
 */
public interface WePluginSupportable {

    /**
     * 注册插件
     *
     * @return 返回的 {@link MenuItem} 将自动添加到插件菜单
     *
     * @since 1.0.0
     */
    MenuItem registerPlugin();
}
