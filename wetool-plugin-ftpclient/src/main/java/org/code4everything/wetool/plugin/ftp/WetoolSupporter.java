package org.code4everything.wetool.plugin.ftp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import lombok.extern.slf4j.Slf4j;
import org.code4everything.wetool.plugin.ftp.config.FtpConfig;
import org.code4everything.wetool.plugin.ftp.config.FtpInfo;
import org.code4everything.wetool.plugin.ftp.constant.FtpConsts;
import org.code4everything.wetool.plugin.ftp.model.LastUsedInfo;
import org.code4everything.wetool.plugin.support.WePluginSupportable;
import org.code4everything.wetool.plugin.support.factory.BeanFactory;
import org.code4everything.wetool.plugin.support.util.FxDialogs;
import org.code4everything.wetool.plugin.support.util.FxUtils;

import java.util.List;

/**
 * @author pantao
 * @since 2019/8/23
 */
@Slf4j
public class WetoolSupporter implements WePluginSupportable {

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
        MenuItem item = new MenuItem(FtpConsts.TAB_NAME);
        if (ftpConfig.getShowOnStartup()) {
            openTab();
        }
        item.setOnAction(e -> openTab());
        return item;
    }

    @Override
    public java.awt.MenuItem registerTrayMenu() {
        java.awt.Menu menu = new java.awt.Menu(FtpConsts.TAB_NAME);
        // 上传文件
        java.awt.MenuItem item = new java.awt.MenuItem(FtpConsts.UPLOAD_FILE);
        item.addActionListener(e -> {
            Node dialogPane = FxUtils.loadFxml(this, "/FtpUploadDialog.fxml");
            FxDialogs.showDialog(null, dialogPane);
        });
        menu.add(item);
        // 下载文件
        item = new java.awt.MenuItem(FtpConsts.DOWNLOAD_FILE);
        item.addActionListener(e -> {
            Node dialogPane = FxUtils.loadFxml(this, "/FtpDownloadDialog.fxml");
            FxDialogs.showDialog(null, dialogPane);
        });
        menu.add(item);
        return menu;
    }

    private void openTab() {
        Platform.runLater(() -> {
            Node node = FxUtils.loadFxml(this, "/FtpTabView.fxml");
            FxUtils.openTab(node, FtpConsts.TAB_ID, FtpConsts.TAB_NAME);
        });
    }
}
