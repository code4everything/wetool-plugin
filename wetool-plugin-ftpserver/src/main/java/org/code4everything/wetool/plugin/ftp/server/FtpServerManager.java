package org.code4everything.wetool.plugin.ftp.server;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import lombok.experimental.UtilityClass;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.code4everything.wetool.plugin.ftp.server.config.FtpServerConfig;
import org.code4everything.wetool.plugin.support.util.FxDialogs;
import org.code4everything.wetool.plugin.support.util.WeUtils;

import java.io.File;

/**
 * @author pantao
 * @since 2019/9/23
 */
@UtilityClass
class FtpServerManager {

    private static final String PATH = "conf" + File.separator + "ftp-server-config.json";

    private static FtpServer server;

    static void start() {
        try {
            if (ObjectUtil.isNull(server) || server.isStopped()) {
                // 读取配置
                FtpServerConfig config = loadConfig();
                if (ObjectUtil.isNull(config) || CollUtil.isEmpty(config.getUsers())) {
                    FxDialogs.showError("FTP启动失败：请完善配置文件！");
                    return;
                }

                // 创建FTP
                FtpServerFactory serverFactory = new FtpServerFactory();
                ListenerFactory factory = new ListenerFactory();
                // 监听端口
                factory.setPort(config.getPort());
                serverFactory.addListener("default", factory.createListener());

                // 添加用户
                UserManager um = serverFactory.getUserManager();
                config.getUsers().forEach(user -> {
                    try {
                        um.save(user);
                    } catch (FtpException e) {
                        FxDialogs.showException("新增用户失败：" + user, e);
                    }
                });

                // 启动服务
                server = serverFactory.createServer();
                server.start();
            } else if (server.isSuspended()) {
                server.resume();
            }
        } catch (Exception e) {
            FxDialogs.showException("FTP服务启动失败！", e);
        }
    }

    static void stop() {
        if (ObjectUtil.isNotNull(server)) {
            try {
                server.stop();
                server = null;
            } catch (Exception e) {
                FxDialogs.showException("停止FTP服务失败！", e);
            }
        }
    }

    static FtpServerConfig loadConfig() {
        String path = WeUtils.parsePathByOs(PATH);
        if (StrUtil.isEmpty(path)) {
            FxDialogs.showError("配置文件：" + path + " 不存在！");
            return null;
        }
        return JSON.parseObject(FileUtil.readUtf8String(path), FtpServerConfig.class);
    }
}
