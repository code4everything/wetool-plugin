package org.code4everything.wetool.plugin.support.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.swing.clipboard.ClipboardUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.code4everything.boot.base.bean.BaseBean;
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

    /**
     * 当前读取的配置文件路径
     */
    private String currentPath;

    private WeInitialize initialize;

    /**
     * 剪贴板列表长度
     */
    private Integer clipboardSize;

    /**
     * 文本框是否自动换行
     */
    private Boolean autoWrap;

    /**
     * 文件过滤
     */
    private String fileFilter;

    /**
     * 初始化选择文件的路径
     */
    private String fileChooserInitDir;

    /**
     * 字符串记录到日志的长度（会压缩裁剪）
     */
    private Integer logCompressLen;

    /**
     * 快速打开
     */
    private List<WeStart> quickStarts;

    /**
     * 此集合内的插件将不会加载
     */
    private Set<WePluginInfo> pluginDisables;

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

    public Pair<Date, String> getLastClipboardHistoryItem() {
        Pair<Date, String> last = clipboardHistory.getLast();
        return ObjectUtil.defaultIfNull(last, new Pair<>(new Date(), StrUtil.nullToEmpty(ClipboardUtil.getStr())));
    }
}
