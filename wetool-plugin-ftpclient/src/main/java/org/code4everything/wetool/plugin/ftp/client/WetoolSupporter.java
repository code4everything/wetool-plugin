package org.code4everything.wetool.plugin.ftp.client;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import lombok.extern.slf4j.Slf4j;
import org.code4everything.wetool.plugin.ftp.client.config.FtpConfig;
import org.code4everything.wetool.plugin.ftp.client.config.FtpInfo;
import org.code4everything.wetool.plugin.ftp.client.constant.FtpConsts;
import org.code4everything.wetool.plugin.ftp.client.model.LastUsedInfo;
import org.code4everything.wetool.plugin.support.WePluginSupporter;
import org.code4everything.wetool.plugin.support.factory.BeanFactory;
import org.code4everything.wetool.plugin.support.util.FxDialogs;
import org.code4everything.wetool.plugin.support.util.FxUtils;

import java.awt.event.ActionListener;
import java.util.List;

/**
 * @author pantao
 * @since 2019/8/23
 */
@Slf4j
public class WetoolSupporter implements WePluginSupporter {

    private FtpConfig ftpConfig;

    @Override
    public boolean initialize() {
        // 获取配置
        ftpConfig = FtpConfig.getConfig();
        List<FtpInfo> ftps = FtpConfig.getFtps(ftpConfig);
        for (FtpInfo ftpInfo : ftps) {
            if (StrUtil.isEmpty(ftpInfo.getName())) {
                // 无效的配置
                log.info("invalid ftp config, the name of ftp connection must not be empty: {}", ftpInfo);
                continue;
            }
            LastUsedInfo.getInstance().addFtpName(ftpInfo.getName());
            if (ftpInfo.getSelect()) {
                LastUsedInfo.getInstance().setFtpName(ftpInfo.getName());
            }
            BeanFactory.register(FtpManager.generateConfigKey(ftpInfo.getName()), ftpInfo);
            // 是否进行初始化连接
            if (ftpInfo.isInitConnect()) {
                ThreadUtil.execute(() -> FtpManager.getFtp(ftpInfo.getName()));
            }
        }
        if (CollUtil.isEmpty(LastUsedInfo.getInstance().getFtpNames())) {
            log.error("ftp config not found, plugin will exit");
            return false;
        }
        return true;
    }

    @Override
    public MenuItem registerBarMenu() {
        if (ftpConfig.getShowOnStartup()) {
            openTab();
        }
        return FxUtils.createMenuItem(FtpConsts.TAB_NAME, (EventHandler<ActionEvent>) e -> openTab());
    }

    @Override
    public java.awt.MenuItem registerTrayMenu() {
        java.awt.Menu menu = new java.awt.Menu(FtpConsts.TAB_NAME);
        // 上传文件
        menu.add(FxUtils.createMenuItem(FtpConsts.UPLOAD_FILE, (ActionListener) e -> {
            Node dialogPane = FxUtils.loadFxml(WetoolSupporter.class, "/ease/ftpclient/FtpUploadDialog.fxml", true);
            FxDialogs.showDialog(null, dialogPane);
        }));
        // 下载文件
        menu.add(FxUtils.createMenuItem(FtpConsts.DOWNLOAD_FILE, (ActionListener) e -> {
            Node dialogPane = FxUtils.loadFxml(WetoolSupporter.class, "/ease/ftpclient/FtpDownloadDialog.fxml", true);
            FxDialogs.showDialog(null, dialogPane);
        }));
        return menu;
    }

    private void openTab() {
        Platform.runLater(() -> {
            Node node = FxUtils.loadFxml(WetoolSupporter.class, "/ease/ftpclient/FtpTabView.fxml", true);
            FxUtils.openTab(node, FtpConsts.TAB_ID, FtpConsts.TAB_NAME);
        });
    }
}
