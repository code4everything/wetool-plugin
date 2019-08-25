package org.code4everything.wetool.plugin.ftp;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import lombok.extern.slf4j.Slf4j;
import org.code4everything.wetool.plugin.ftp.config.FtpConfig;
import org.code4everything.wetool.plugin.ftp.constant.FtpConsts;
import org.code4everything.wetool.plugin.ftp.model.LastUsedInfo;
import org.code4everything.wetool.plugin.support.WePluginSupportable;
import org.code4everything.wetool.plugin.support.factory.BeanFactory;
import org.code4everything.wetool.plugin.support.util.FxDialogs;
import org.code4everything.wetool.plugin.support.util.FxUtils;
import org.code4everything.wetool.plugin.support.util.WeUtils;

/**
 * @author pantao
 * @since 2019/8/23
 */
@Slf4j
public class WetoolSupporter implements WePluginSupportable {

    @Override
    public void initialize() {
        JSONArray ftps = WeUtils.getConfig().getConfig("ftps", JSONArray.class);
        for (int i = 0; i < ftps.size(); i++) {
            FtpConfig config = ftps.getObject(i, FtpConfig.class);
            if (StrUtil.isEmpty(config.getName())) {
                // 无效的配置
                log.info("invalid ftp config, the name of ftp connection must not be empty: {}", config);
                continue;
            }
            LastUsedInfo.getInstance().addFtpName(config.getName());
            if (config.getSelect()) {
                LastUsedInfo.getInstance().setDefaultFtp(config.getName());
            }
            BeanFactory.register(FtpManager.generateConfigKey(config.getName()), config);
        }
    }

    @Override
    public MenuItem registerBarMenu() {
        return null;
    }

    @Override
    public java.awt.MenuItem registerTrayMenu() {
        java.awt.Menu menu = new java.awt.Menu(FtpConsts.FTP);
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
}
