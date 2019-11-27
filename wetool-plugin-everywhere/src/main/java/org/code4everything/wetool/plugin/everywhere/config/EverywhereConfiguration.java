package org.code4everything.wetool.plugin.everywhere.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import lombok.*;
import lombok.experimental.Accessors;
import org.code4everything.boot.base.FileUtils;
import org.code4everything.boot.base.bean.BaseBean;
import org.code4everything.wetool.plugin.support.util.WeUtils;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author pantao
 * @since 2019/11/26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class EverywhereConfiguration implements BaseBean {

    private static final String CONFIG_FILE = StrUtil.join(File.separator, "conf", "everywhere-lucene-config.json");

    private static Formatted formatted = new Formatted();

    /**
     * 需要创建内容索引的文件名（正则匹配）
     */
    private Set<String> includeFilenames;

    /**
     * 不创建内容索引的文件名（正则匹配）
     */
    private Set<String> excludeFilenames;

    private Boolean ignoreHiddenFile;

    /**
     * 超过设置大小的文件不创建内容索引，最大100MB(10,000,000)，格式：1,000,000，单位：B
     */
    private String sizeLimit;

    public static Formatted getFormatted() {
        return formatted;
    }

    public static Formatted loadConfiguration() {
        String path = WeUtils.parsePathByOs(CONFIG_FILE);
        if (StrUtil.isNotBlank(path)) {
            String json = FileUtil.readUtf8String(path);
            try {
                JSON.parseObject(json, EverywhereConfiguration.class);
            } catch (Exception e) {
                // ignore
            }
        }
        return formatted;
    }

    public static String getPath() {
        return StrUtil.emptyToDefault(WeUtils.parsePathByOs(CONFIG_FILE), FileUtils.currentWorkDir(CONFIG_FILE));
    }

    public void setIncludeFilenames(Set<String> includeFilenames) {
        this.includeFilenames = includeFilenames;
        formatted.includeFilenames = toPatterns(includeFilenames);
    }

    public void setExcludeFilenames(Set<String> excludeFilenames) {
        this.excludeFilenames = excludeFilenames;
        formatted.addExcluded(excludeFilenames);
        formatted.excludeFilenames = toPatterns(excludeFilenames);
    }

    public void setIgnoreHiddenFile(Boolean ignoreHiddenFile) {
        this.ignoreHiddenFile = ignoreHiddenFile;
        formatted.ignoreHiddenFile = ObjectUtil.defaultIfNull(ignoreHiddenFile, true);
    }

    public void setSizeLimit(String sizeLimit) {
        this.sizeLimit = sizeLimit;
        String size = sizeLimit.replaceAll("[,_\\s]", "");
        if (NumberUtil.isNumber(size)) {
            formatted.sizeLimit = NumberUtil.parseInt(size);
        }
    }

    private List<Pattern> toPatterns(Set<String> patterns) {
        if (CollUtil.isEmpty(patterns)) {
            return Collections.emptyList();
        }
        List<Pattern> patternList = new ArrayList<>(patterns.size());
        patterns.forEach(p -> {
            try {
                patternList.add(Pattern.compile(p));
            } catch (Exception e) {
                // ignore
            }
        });
        return patternList;
    }

    @ToString
    public static class Formatted {

        private Set<String> excluded = new HashSet<>();

        @Getter
        private List<Pattern> includeFilenames = Collections.emptyList();

        @Getter
        private List<Pattern> excludeFilenames = Collections.emptyList();

        @Getter
        private boolean ignoreHiddenFile = true;

        @Getter
        private int sizeLimit = 10_000_000;

        private void addExcluded(Set<String> excluded) {
            if (CollUtil.isEmpty(excluded)) {
                return;
            }
            this.excluded.addAll(excluded);
        }

        public void addExcludeFilenames(File file) {
            if (!FileUtil.exist(file)) {
                return;
            }
            if (CollUtil.isEmpty(excludeFilenames)) {
                excludeFilenames = new ArrayList<>();
            }
            final String pattern = StrUtil.emptyToDefault(FileUtil.extName(file), FileUtil.getName(file));
            if (excluded.add(pattern)) {
                excludeFilenames.add(Pattern.compile(pattern));
            }
        }
    }
}
