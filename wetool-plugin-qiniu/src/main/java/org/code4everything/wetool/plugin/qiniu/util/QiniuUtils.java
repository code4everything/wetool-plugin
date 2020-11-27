package org.code4everything.wetool.plugin.qiniu.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.code4everything.boot.base.FileUtils;
import org.code4everything.boot.base.constant.IntegerConsts;
import org.code4everything.boot.base.constant.StringConsts;
import org.code4everything.wetool.plugin.qiniu.constant.QiniuConsts;
import org.code4everything.wetool.plugin.qiniu.model.ConfigBean;
import org.code4everything.wetool.plugin.support.util.WeUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.regex.Pattern;

/**
 * @author pantao
 * @since 2018/4/14
 */
@Slf4j
@UtilityClass
public class QiniuUtils {

    /**
     * 日期匹配
     */
    public static final Pattern DATE_PATTERN = Pattern.compile("^[0-9]{4}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])$");

    /**
     * 匹配字符串是否有数字
     */
    private static final Pattern HAS_DIGIT_PATTERN = Pattern.compile(".*[0-9]+.*");

    private static final String HTTP = "http";

    /**
     * 单位KB
     */
    private static final String SIZE_KB = "KB";

    /**
     * 单位MB
     */
    private static final String SIZE_MB = "MB";

    /**
     * 单位GB
     */
    private static final String SIZE_GB = "GB";

    /**
     * 单位TB
     */
    private static final String SIZE_TB = "TB";

    /**
     * 是否为日期格式
     *
     * @param date 需要判断的日期
     *
     * @return {@link Boolean}
     */
    public static boolean isDate(String date) {
        return StrUtil.isNotEmpty(date) && DATE_PATTERN.matcher(date).matches();
    }

    /**
     * 抽取字符串的数字（包括最后一个点号）
     *
     * @param string {@link String}
     *
     * @return {@link String}
     */
    public static String extractDigit(String string) {
        StringBuilder res = new StringBuilder();
        if (HAS_DIGIT_PATTERN.matcher(string).matches()) {
            string = string.replaceAll("(\\s|[a-zA-Z])+", "");
            res = new StringBuilder(string.indexOf('-') == 0 ? "-" : "");
            int dotIdx = string.lastIndexOf('.');
            for (int i = 0; i < string.length(); i++) {
                char c = string.charAt(i);
                if (Character.isDigit(c) || i == dotIdx) {
                    res.append(c);
                }
            }
            if (res.indexOf(StringConsts.Sign.DOT) == 0) {
                res.insert(0, "0");
            } else if (res.indexOf("-.") == 0) {
                res = new StringBuilder("-0." + res.substring(2, res.length()));
            }
        }
        return res.toString();
    }

    /**
     * 将格式化后的大小转换成long型
     *
     * @param size 格式为 xx.xx B、xx.xx KB、xx.xx MB、xx.xx GB、xx.xx TB 的字符串
     *
     * @return 单位为B的{@link Long}
     */
    public static long sizeToLong(String size) {
        size = size.trim();
        if (StrUtil.isNotEmpty(size)) {
            String num;
            if (size.contains(StringConsts.Sign.SPACE)) {
                num = size.split(StringConsts.Sign.SPACE)[0];
            } else {
                num = extractDigit(size);
            }
            double result;
            if (size.contains(SIZE_TB)) {
                result = Double.parseDouble(num) * IntegerConsts.FileSize.TB;
            } else if (size.contains(SIZE_GB)) {
                result = Double.parseDouble(num) * IntegerConsts.FileSize.GB;
            } else if (size.contains(SIZE_MB)) {
                result = Double.parseDouble(num) * IntegerConsts.FileSize.MB;
            } else if (size.contains(SIZE_KB)) {
                result = Double.parseDouble(num) * IntegerConsts.FileSize.KB;
            } else {
                result = Double.parseDouble(num);
            }
            return (long) result;
        }
        return -1;
    }

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
                    FileUtils.currentWorkDir());
            if (Validator.isEmpty(storagePath)) {
                return;
            }
            ConfigBean.getConfig().setStoragePath(storagePath);
            ConfigUtils.writeConfig();
        }
        final String dest = ConfigBean.getConfig().getStoragePath();
        // 下载文件
        WeUtils.execute(() -> HttpUtil.downloadFile(url, dest));
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
            log.error("there is no connection to the network");
            return false;
        }
    }

    public static void saveFile(File file, String content) {
        try {
            FileUtil.writeUtf8String(content, file);
        } catch (Exception e) {
            Alerts.showError(QiniuConsts.TAB_NAME, e.getMessage());
        }
    }

    /**
     * 拼接链接
     */
    public static String buildUrl(String fileName, String domain) {
        if (!domain.startsWith(HTTP)) {
            domain = HTTP + "://" + domain;
        }
        fileName = fileName.replace(" ", "qn_code_per_20").replace("/", "qn_code_per_2F");
        fileName = URLEncoder.encode(fileName, CharsetUtil.CHARSET_UTF_8).replace("qn_code_per_2F", "/");
        return String.format("%s/%s", domain, fileName.replace("qn_code_per_20", "%20"));
    }
}
