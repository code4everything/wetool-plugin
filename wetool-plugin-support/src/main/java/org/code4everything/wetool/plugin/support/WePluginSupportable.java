package org.code4everything.wetool.plugin.support;

import javafx.scene.control.MenuItem;

/**
 * 工具插件接口，实现类必须包含一个无参构造函数
 *
 * @author pantao
 * @since 2019/8/22
 */
public interface WePluginSupportable {

    /**
     * 初始化操作
     */
    default void initialize() {}

    /**
     * 注册插件到主界面菜单
     *
     * @return 返回的 {@link MenuItem} 将被添加到主界面的插件菜单
     *
     * @since 1.0.0
     */
    default MenuItem registerBarMenu() {return null;}

    /**
     * 注册插件到系统托盘菜单
     *
     * @return 返回的 {@link MenuItem} 将被添加到系统托盘的插件菜单
     *
     * @since 1.0.0
     */
    default java.awt.MenuItem registerTrayMenu() {return null;}
}
