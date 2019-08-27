package org.code4everything.wetool.plugin.support.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.code4everything.boot.base.constant.IntegerConsts;
import org.code4everything.wetool.plugin.support.config.WeConfig;
import org.code4everything.wetool.plugin.support.factory.BeanFactory;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author pantao
 * @since 2018/3/31
 */
@Slf4j
@UtilityClass
public class WeUtils {

    private static final String TIME_VARIABLE = "%(TIME|time)%";

    private static final String DATE_VARIABLE = "%(DATE|date)%";

    private static int compressLen = 0;

    /**
     * 获取用户的配置
     */
    public static WeConfig getConfig() {
        return BeanFactory.get(WeConfig.class);
    }

    /**
     * 检测版本是否要求
     *
     * @param currVer 当前版本
     * @param reqVer 要求的最低版本
     */
    public static boolean isRequiredVersion(String currVer, String reqVer) {
        String[] currArr = currVer.split("\\.");
        String[] reqArr = reqVer.split("\\.");
        int len = Math.max(currArr.length, reqArr.length);
        for (int i = 0; i < len; i++) {
            int curr = i < currArr.length ? Integer.parseInt(currArr[i]) : 0;
            int req = i < reqArr.length ? Integer.parseInt(reqArr[i]) : 0;
            if (curr > req) {
                return true;
            }
            if (curr < req) {
                return false;
            }
        }
        return true;
    }

    /**
     * 压缩字符串
     */
    public static String compressString(String string) {
        string = string.trim();
        if (string.length() > getCompressLen()) {
            string = string.substring(0, getCompressLen()) + "......";
        }
        return string.replaceAll("(\\s{2,}|\r\n|\r|\n)", " ");
    }

    /**
     * 将目标文件列表中的文件以递归的方式添加到源文件列表，添加过程将使用用户配置过滤规则来过滤文件
     *
     * @param src 源文件列表
     * @param adds 目标文件列表
     */
    public static void addFiles(List<File> src, List<File> adds) {
        if (CollUtil.isEmpty(adds)) {
            return;
        }
        WeConfig config = getConfig();
        for (File file : adds) {
            if (!config.getFilterPattern().matcher(file.getName()).matches()) {
                // 文件不匹配
                log.info("filter file: {}", file.getAbsolutePath());
                continue;
            }
            if (file.isFile() && !src.contains(file)) {
                src.add(file);
            } else if (file.isDirectory()) {
                addFiles(src, CollUtil.newArrayList(file.listFiles()));
            }
        }
    }

    /**
     * 如果文件是文件夹，则直接返回其路径，否则返回其上级节点路径
     */
    public static String parseFolder(File file) {
        return file.isDirectory() ? file.getAbsolutePath() : file.getParent();
    }

    /**
     * 替换变量%(TIME|time)%和"%(DATE|date)%"
     *
     * @param str 需要替换的字符串
     */
    public static String replaceVariable(String str) {
        str = StrUtil.nullToEmpty(str);
        if (StrUtil.isNotEmpty(str)) {
            Date date = new Date();
            str = str.replaceAll(DATE_VARIABLE, DateUtil.formatDate(date));
            str = str.replaceAll(TIME_VARIABLE, DateUtil.formatTime(date));
        }
        return str;
    }

    /**
     * 字符串转整型
     *
     * @param num 字符串
     * @param minVal 最小值
     */
    public static int parseInt(String num, int minVal) {
        int n = 0;
        if (NumberUtil.isNumber(num)) {
            n = NumberUtil.parseInt(num);
        }
        return Math.max(n, minVal);
    }

    /**
     * 退出系统
     */
    public static void exitSystem() {
        log.info("wetool exited");
        System.exit(IntegerConsts.ZERO);
    }

    private static int getCompressLen() {
        if (compressLen < 1) {
            Integer val = getConfig().getLogCompressLen();
            compressLen = Objects.isNull(val) || val < 1 ? 64 : val;
        }
        return compressLen;
    }
}
