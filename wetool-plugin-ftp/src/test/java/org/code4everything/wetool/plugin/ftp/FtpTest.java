package org.code4everything.wetool.plugin.ftp;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.code4everything.wetool.plugin.ftp.config.FtpConfig;
import org.code4everything.wetool.plugin.support.config.WeConfig;
import org.code4everything.wetool.plugin.test.WetoolTest;

/**
 * @author pantao
 * @since 2019/8/24
 */
public class FtpTest {

    public static void main(String[] args) {
        // 模拟配置文件
        WeConfig config = WetoolTest.getConfig();
        JSONObject json = new JSONObject();
        JSONArray ftps = new JSONArray();

        FtpConfig ftpConfig = new FtpConfig();
        ftpConfig.setName("test");
        ftpConfig.setCharset("utf-8");
        ftpConfig.setAnonymous(false);
        ftpConfig.setHost("192.168.1.234");
        ftpConfig.setPort(21);
        ftpConfig.setUsername("root");
        ftpConfig.setPassword("root");
        ftpConfig.setReconnect(false);
        ftpConfig.setSelect(true);

        ftps.add(ftpConfig);
        json.put("ftps", ftps);
        config.setConfigJson(json);

        WetoolTest.runTest(new WetoolSupporter(), config, args);
    }
}
