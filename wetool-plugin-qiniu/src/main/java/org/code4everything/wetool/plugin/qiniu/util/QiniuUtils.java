package org.code4everything.wetool.plugin.qiniu.util;

import cn.hutool.core.lang.Validator;
import cn.hutool.http.HttpUtil;
import com.zhazhapan.util.FileExecutor;
import com.zhazhapan.util.Formatter;
import com.zhazhapan.util.ThreadPool;
import com.zhazhapan.util.Utils;
import com.zhazhapan.util.dialog.Alerts;
import org.apache.log4j.Logger;
import org.code4everything.wetool.plugin.qiniu.constant.QiniuConsts;
import org.code4everything.wetool.plugin.qiniu.model.ConfigBean;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * @author pantao
 * @since 2018/4/14
 */
public class QiniuUtils {

    private static final Logger LOGGER = Logger.getLogger(QiniuUtils.class);

    private static final String HTTP = "http";

    private QiniuUtils() {}

    /**
     * 下载文件
     *
     * @param url 文件链接
     */
    public static void download(String url) {
        // 验证存储路径
        if (Validator.isEmpty(ConfigBean.getConfig().getStoragePath())) {
            // 显示存储路径输入框
            String storagePath = DialogUtils.showInputDialog(null, QiniuConsts.CONFIG_DOWNLOAD_PATH,
                    Utils.getCurrentWorkDir());
            if (Validator.isEmpty(storagePath)) {
                return;
            }
            ConfigBean.getConfig().setStoragePath(storagePath);
            ConfigUtils.writeConfig();
        }
        final String dest = ConfigBean.getConfig().getStoragePath();
        // 下载文件
        ThreadPool.executor.execute(() -> HttpUtil.downloadFile(url, dest));
    }

    /**
     * 检查是否连接网络
     */
    public static boolean checkNet() {
        try {
            URL url = new URL("https://www.qiniu.com/");
            InputStream in = url.openStream();
            in.close();
            return true;
        } catch (IOException e) {
            LOGGER.error("there is no connection to the network");
            return false;
        }
    }

    public static void saveLogFile(String file, String content) {
        try {
            FileExecutor.saveLogFile(file, content);
        } catch (IOException e) {
            Alerts.showError(QiniuConsts.TAB_NAME, e.getMessage());
        }
    }

    public static void saveFile(File file, String content) {
        try {
            FileExecutor.saveFile(file, content);
        } catch (IOException e) {
            Alerts.showError(QiniuConsts.TAB_NAME, e.getMessage());
        }
    }

    public static void openLink(String url) {
        try {
            Utils.openLink(url);
        } catch (Exception e) {
            Alerts.showError(QiniuConsts.TAB_NAME, e.getMessage());
        }
    }

    public static String getFileName(String string) {
        try {
            return Formatter.getFileName(string);
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("get file name of url failed, message -> " + e.getMessage());
            return "";
        }
    }

    /**
     * 拼接链接
     */
    public static String buildUrl(String fileName, String domain) {
        if (!domain.startsWith(HTTP)) {
            domain = HTTP + "://" + domain;
        }
        fileName = fileName.replaceAll(" ", "qn_code_per_20").replaceAll("/", "qn_code_per_2F");
        try {
            fileName = URLEncoder.encode(fileName, "utf-8").replaceAll("qn_code_per_2F", "/");
            return String.format("%s/%s", domain, fileName.replaceAll("qn_code_per_20", "%20"));
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("encode url failed, message -> " + e.getMessage());
            return "";
        }
    }
}
