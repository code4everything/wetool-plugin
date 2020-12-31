package org.code4everything.wetool.plugin.support.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.swing.clipboard.ClipboardUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.code4everything.boot.base.bean.BaseBean;
import org.code4everything.boot.config.BootConfig;
import org.code4everything.wetool.plugin.support.constant.AppConsts;
import org.code4everything.wetool.plugin.support.event.message.MouseCornerEventMessage;
import org.code4everything.wetool.plugin.support.util.Callable;
import org.code4everything.wetool.plugin.support.util.WeUtils;

import java.io.Serializable;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author pantao
 * @since 2019/7/3
 **/
@Data
@Slf4j
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class WeConfig implements BaseBean, Serializable {

    private static final long serialVersionUID = 6105929832284264685L;

    private String currentPath;

    private boolean debug = false;

    // start------------------------------------------------------------------------------------------------------------

    /**
     * 初始化参数
     */
    private WeInitialize initialize = new WeInitialize();

    /**
     * 剪贴板历史记录的条数
     */
    private Integer clipboardSize = 20;

    /**
     * 文本框是否自动换行
     */
    private Boolean autoWrap = true;

    /**
     * 文件过滤（正则表达式）
     */
    private String fileFilter = "";

    /**
     * 初始化选择文件的路径
     */
    private String fileChooserInitDir = "";

    /**
     * 字符串记录到日志的长度（会压缩裁剪）
     */
    private Integer logCompressLen = 128;

    /**
     * 可快捷打开文件的一系列菜单
     */
    private Set<WeStart> quickStarts = Collections.emptySet();

    /**
     * 禁止加载的插件，禁止的插件只需配置插件的作者、名称、版本即可（这些信息可在程序刚启动时的日志中看到）
     */
    private Set<WePluginInfo> pluginDisables = Collections.emptySet();

    /**
     * 插件路径
     */
    private Set<String> pluginPaths = Collections.emptySet();

    /**
     * windows虚拟桌面触发角
     */
    private MouseCornerEventMessage.LocationTypeEnum winVirtualDesktopHotCorner =
            MouseCornerEventMessage.LocationTypeEnum.NONE;

    /**
     * 自定义重启脚本文件名，只能位于WeTool工作目录
     */
    private String restartBatch = "";

    /**
     * 数据库连接配置，最好带上name属性
     * <p>
     * 参考：https://github.com/alibaba/druid/wiki/DruidDataSource%E9%85%8D%E7%BD%AE%E5%B1%9E%E6%80%A7%E5%88%97%E8%A1%A8
     */
    private JSONArray dbConnections = new JSONArray();

    /**
     * 是否关闭鼠标键盘监听器
     */
    private Boolean disableKeyboardMouseListener = false;

    /**
     * 是否关闭暗黑模式
     */
    private Boolean disableDarkMode = false;

    /**
     * 自动移除未加载的插件
     */
    private Boolean autoRemoveUnloadedPlugin = true;

    // end--------------------------------------------------------------------------------------------------------------

    private transient LinkedList<Pair<Date, String>> clipboardHistory = new LinkedList<>();

    private transient Pattern filterPattern = Pattern.compile("");

    private transient JSONObject configJson;

    @Override
    public void init() {
        try {
            requireNonNullProperty();
            initialize.requireNonNullProperty();
            initialize.getTabs().requireNonNullProperty();
            if (CollUtil.isNotEmpty(quickStarts)) {
                quickStarts.forEach(WeStart::requireNonNullProperty);
            }
        } catch (Exception e) {
            log.error("config file format error: {}", e.getMessage());
            WeUtils.exitSystem();
        }
        filterPattern = null;
        BootConfig.setDebug(debug);
    }

    /**
     * 获取自定义配置
     *
     * @param path FastJson路径语法
     */
    public <T> T getConfig(String path, Class<T> clazz) {
        return JSON.parseObject(JSON.toJSONString(getConfig(path)), clazz);
    }

    /**
     * 获取自定义配置
     *
     * @param path FastJson路径语法
     */
    public Object getConfig(String path) {
        Object object = JSONPath.eval(configJson, path);
        if (Objects.isNull(object)) {
            // 重新加载配置
            configJson = JSON.parseObject(FileUtil.readUtf8String(currentPath));
            object = JSONPath.eval(configJson, path);
        }
        return object;
    }

    /**
     * 禁止外部访问
     */
    @Generated
    private JSONObject getConfigJson() {
        return configJson;
    }

    @Generated
    public Pattern getFilterPattern() {
        if (Objects.isNull(filterPattern)) {
            filterPattern = Pattern.compile(fileFilter);
        }
        return filterPattern;
    }

    /**
     * 禁止外部设置
     */
    @Generated
    private void setFilterPattern(Pattern filterPattern) {
        this.filterPattern = filterPattern;
    }

    @Generated
    public String getFileChooserInitDir() {
        return StrUtil.emptyToDefault(fileChooserInitDir, FileUtil.getUserHomePath());
    }

    public void appendClipboardHistory(Date date, String content) {
        if (clipboardHistory.size() < clipboardSize) {
            clipboardHistory.add(new Pair<>(date, StrUtil.nullToEmpty(content)));
        } else {
            clipboardHistory.removeFirst();
            appendClipboardHistory(date, content);
        }
    }

    @JSONField(serialize = false)
    public Pair<Date, String> getLastClipboardHistoryItem() {
        Pair<Date, String> last = clipboardHistory.getLast();
        return ObjectUtil.defaultIfNull(last, new Pair<>(new Date(), StrUtil.nullToEmpty(ClipboardUtil.getStr())));
    }

    public void darkIfEnabled(Callable<String> darkCallable) {
        if (BooleanUtil.isTrue(getDisableDarkMode())) {
            return;
        }
        darkCallable.call(AppConsts.DARK_CSS);
    }
}
