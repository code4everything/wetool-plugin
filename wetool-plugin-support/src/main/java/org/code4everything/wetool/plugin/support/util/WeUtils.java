package org.code4everything.wetool.plugin.support.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.system.OsInfo;
import cn.hutool.system.SystemUtil;
import com.alibaba.druid.pool.DruidDataSource;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.code4everything.boot.base.FileUtils;
import org.code4everything.boot.base.constant.IntegerConsts;
import org.code4everything.boot.config.BootConfig;
import org.code4everything.wetool.plugin.support.config.WeConfig;
import org.code4everything.wetool.plugin.support.druid.DruidSource;
import org.code4everything.wetool.plugin.support.event.EventCenter;
import org.code4everything.wetool.plugin.support.factory.BeanFactory;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author pantao
 * @since 2018/3/31
 */
@Slf4j
@UtilityClass
public class WeUtils {

    private static final String TIME_VARIABLE = "%(TIME|time)%";

    private static final String DATE_VARIABLE = "%(DATE|date)%";

    private static final String PREFIX = "wetool-pool-";

    private static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {

        private final AtomicInteger threadNumber = new AtomicInteger(1);

        private final Thread.UncaughtExceptionHandler exceptionHandler =
                (t, e) -> log.error(ExceptionUtil.stacktraceToString(e, Integer.MAX_VALUE));

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, PREFIX + threadNumber.getAndIncrement());
            thread.setDaemon(true);
            thread.setUncaughtExceptionHandler(exceptionHandler);
            return thread;
        }
    };

    private static final ThreadPoolExecutor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(16, 32, 60L,
            TimeUnit.SECONDS, new LinkedBlockingQueue<>(512), THREAD_FACTORY);

    private static int compressLen = 0;

    /**
     * 获取当前进程id
     *
     * @since 1.6.0
     */
    public static int getCurrentPid() {
        return NumberUtil.parseInt(StrUtil.split(ManagementFactory.getRuntimeMXBean().getName(), "@")[0]);
    }

    /**
     * 异步执行
     *
     * @since 1.3.0
     */
    public static void execute(Runnable runnable) {
        THREAD_POOL_EXECUTOR.execute(runnable);
    }

    /**
     * 异步执行
     *
     * @since 1.3.0
     */
    public static <V> Future<V> executeAsync(Callable<V> callable) {
        return THREAD_POOL_EXECUTOR.submit(callable);
    }

    /**
     * 获取插件目录
     *
     * @return 插件目录
     *
     * @since 1.2.0
     */
    public static File getPluginFolder() {
        String pluginDir = FileUtils.currentWorkDir("plugins");
        return FileUtil.mkdir(pluginDir);
    }

    /**
     * 解析文件路径
     *
     * @param filename 默认文件名
     *
     * @return 文件路径
     *
     * @since 1.0.1
     */
    public static String parsePathByOs(String filename) {
        return parsePathByOs(FileUtils.currentWorkDir(), filename);
    }

    /**
     * 解析文件路径
     *
     * @param parentDir 父文件夹
     * @param filename 默认文件名
     *
     * @return 文件路径
     *
     * @since 1.0.1
     */
    public static String parsePathByOs(String parentDir, String filename) {
        int idx = filename.lastIndexOf('.');
        String name = filename.substring(0, idx);
        String ext = filename.substring(idx);
        return parsePathByOs(parentDir, name + "-win" + ext, name + "-mac" + ext, name + "-lin" + ext, filename);
    }

    /**
     * 解析文件路径
     *
     * @param parentDir 父文件夹
     * @param winFile Windows文件
     * @param macFile Mac文件
     * @param linFile Linux文件
     * @param defaultFile 默认文件
     *
     * @return 文件路径
     *
     * @since 1.0.1
     */
    public static String parsePathByOs(String parentDir, String winFile, String macFile, String linFile,
                                       String defaultFile) {
        OsInfo osInfo = SystemUtil.getOsInfo();
        parentDir = StrUtil.addSuffixIfNot(parentDir, File.separator);
        // Windows配置文件
        String winPath = parentDir + winFile;
        // Mac配置文件
        String macPath = parentDir + macFile;
        // Linux配置文件
        String linPath = parentDir + linFile;
        // 默认配置文件
        String defPath = parentDir + defaultFile;

        // 解析正确的配置文件路径
        String path = null;
        if (osInfo.isWindows() && FileUtil.exist(winPath)) {
            path = winPath;
        } else if (osInfo.isMac() && FileUtil.exist(macPath)) {
            path = macPath;
        } else if (osInfo.isLinux() && FileUtil.exist(linPath)) {
            path = linPath;
        } else if (FileUtil.exist(defPath)) {
            path = defPath;
        }
        return path;
    }

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
        EventCenter.publishEvent(EventCenter.EVENT_WETOOL_EXIT, DateUtil.date());
        DruidSource.listAllDataSources().forEach(DruidDataSource::close);
        THREAD_POOL_EXECUTOR.shutdown();
        log.info("wetool exited");
        System.exit(IntegerConsts.ZERO);
    }

    /**
     * 打印调试
     */
    public static void printDebug(String msg, Object... objects) {
        if (BootConfig.isDebug()) {
            String message = StrUtil.format(msg, objects);
            Console.log(message);
            log.warn(message);
        }
    }

    private static int getCompressLen() {
        if (compressLen < 1) {
            Integer val = getConfig().getLogCompressLen();
            compressLen = Objects.isNull(val) || val < 1 ? 64 : val;
        }
        return compressLen;
    }
}
