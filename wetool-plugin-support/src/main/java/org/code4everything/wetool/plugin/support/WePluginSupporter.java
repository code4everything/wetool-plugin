package org.code4everything.wetool.plugin.support;

import javafx.scene.control.MenuItem;
import org.code4everything.wetool.plugin.support.config.WePluginInfo;

/**
 * 工具插件接口，实现类必须包含一个无参构造函数
 *
 * @author pantao
 * @since 2019/8/22
 */
public interface WePluginSupporter {

    /**
     * 初始化操作
     *
     * @return 初始化是否成功，返回true时继续加载插件，否则放弃加载
     */
    default boolean initialize() {return true;}

    /**
     * 注册插件到主界面菜单，可返回NULL，可不实现此方法
     *
     * @return 返回的 {@link MenuItem} 将被添加到主界面的插件菜单
     *
     * @since 1.0.0
     */
    default MenuItem registerBarMenu() {return null;}

    /**
     * 注册插件到系统托盘菜单，可返回NULL，可不实现此方法
     *
     * @return 返回的 {@link MenuItem} 将被添加到系统托盘的插件菜单
     *
     * @since 1.0.0
     */
    default java.awt.MenuItem registerTrayMenu() {return null;}

    /**
     * 注册成功之后的回调
     *
     * @param info 定义的插件信息
     * @param barMenu 注册的主界面菜单
     * @param trayMenu 注册的托盘菜单
     */
    default void registered(WePluginInfo info, MenuItem barMenu, java.awt.MenuItem trayMenu) {}

    /**
     * 开发调试时调用（最后调用）
     *
     * @since 1.0.2
     */
    default void debugCall() {}
}
