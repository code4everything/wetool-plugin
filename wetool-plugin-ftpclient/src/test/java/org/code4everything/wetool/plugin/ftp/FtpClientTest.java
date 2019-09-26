package org.code4everything.wetool.plugin.ftp;

import com.alibaba.fastjson.JSONObject;
import org.code4everything.wetool.plugin.ftp.config.FtpConfig;
import org.code4everything.wetool.plugin.ftp.config.FtpInfo;
import org.code4everything.wetool.plugin.support.config.WeConfig;
import org.code4everything.wetool.plugin.test.WetoolTester;

/**
 * @author pantao
 * @since 2019/8/24
 */
public class FtpClientTest {

    public static void main(String[] args) {
        // 模拟配置文件
        WeConfig config = WetoolTester.getConfig();
        JSONObject json = new JSONObject();
        FtpConfig ftpConfig = new FtpConfig();
        ftpConfig.setShowOnStartup(true);

        FtpInfo ftpInfo = new FtpInfo();
        ftpInfo.setName("test");
        ftpInfo.setCharset("utf-8");
        ftpInfo.setAnonymous(false);
        ftpInfo.setHost("192.168.1.234");
        ftpInfo.setPort(21);
        ftpInfo.setUsername("root");
        ftpInfo.setPassword("root");
        ftpInfo.setSelect(true);
        ftpInfo.setLazyConnect(false);

        ftpConfig.addFtp(ftpInfo);
        json.put("easeFtp", ftpConfig);
        config.setConfigJson(json);

        WetoolTester.runTest(new WetoolSupporter(), config, args);
    }
}
