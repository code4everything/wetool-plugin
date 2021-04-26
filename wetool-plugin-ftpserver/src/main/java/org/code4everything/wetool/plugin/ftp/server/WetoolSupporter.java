package org.code4everything.wetool.plugin.ftp.server;

import cn.hutool.core.util.ObjectUtil;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import org.code4everything.wetool.plugin.ftp.server.config.FtpServerConfig;
import org.code4everything.wetool.plugin.support.WePluginSupporter;

/**
 * @author pantao
 * @since 2019/9/23
 */
public class WetoolSupporter implements WePluginSupporter {

    private static final String TITLE = "简易FTP服务器";

    private static final String START = "启动";

    private static final String STOP = "停止";

    @Override
    public boolean initialize() {
        FtpServerConfig config = FtpServerManager.loadConfig();
        if (ObjectUtil.isNotNull(config) && config.getStartOnStartup()) {
            FtpServerManager.start();
        }
        return true;
    }

    @Override
    public MenuItem registerBarMenu() {
        Menu menu = new Menu(TITLE);
        // 启动
        MenuItem item = new MenuItem(START);
        item.setOnAction(e -> FtpServerManager.start());
        menu.getItems().add(item);
        // 停止
        item = new MenuItem(STOP);
        item.setOnAction(e -> FtpServerManager.stop());
        menu.getItems().add(item);
        return menu;
    }

    @Override
    public java.awt.MenuItem registerTrayMenu() {
        java.awt.Menu menu = new java.awt.Menu(TITLE);
        // 启动
        java.awt.MenuItem item = new java.awt.MenuItem(START);
        item.addActionListener(e -> FtpServerManager.start());
        menu.add(item);
        // 停止
        item = new java.awt.MenuItem(STOP);
        item.addActionListener(e -> FtpServerManager.stop());
        menu.add(item);
        return menu;
    }

    @Override
    public void initBootIfConfigured() {
        FtpServerManager.start();
    }
}
