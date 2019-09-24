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

/**
 * @author pantao
 * @since 2019/9/23
 */
@UtilityClass
class FtpServerManager {

    private static FtpServer server;

    static void start() {
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
            factory.setPort(2221);

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
            try {
                server.start();
            } catch (FtpException e) {
                FxDialogs.showException("FTP服务启动失败！", e);
            }
        } else if (server.isSuspended()) {
            server.resume();
        }
    }

    static void stop() {
        if (ObjectUtil.isNotNull(server)) {
            server.stop();
            server = null;
        }
    }

    static FtpServerConfig loadConfig() {
        String path = WeUtils.parsePathByOs("ftp-server-config.json");
        if (StrUtil.isEmpty(path)) {
            FxDialogs.showError("配置文件：ftp-server-config.json 不存在！");
            return null;
        }
        return JSON.parseObject(FileUtil.readUtf8String(path), FtpServerConfig.class);
    }
}
