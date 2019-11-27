package org.code4everything.wetool.plugin.everywhere.lucene.filter;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import org.code4everything.wetool.plugin.everywhere.config.EverywhereConfiguration;
import org.code4everything.wetool.plugin.everywhere.constant.CommonConsts;
import org.code4everything.wetool.plugin.everywhere.util.FileTypeUtils;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 文件内容索引过滤器
 *
 * @author pantao
 * @since 2019/11/26
 */
public class ContentFilter implements IndexFilter {

    private final Map<String, Boolean> indexCache = new HashMap<>(64);

    @Override
    public boolean shouldIndex(File file) {
        if (!FileUtil.exist(file)) {
            return false;
        }
        if (file.isDirectory()) {
            return false;
        }
        String filename = file.getName();
        EverywhereConfiguration.Formatted formatted = EverywhereConfiguration.getFormatted();
        List<Pattern> excludes = formatted.getExcludeFilenames();
        for (Pattern exclude : excludes) {
            if (exclude.matcher(filename).find()) {
                return false;
            }
        }

        long size = FileUtil.size(file);
        if (size > CommonConsts.MAX_FILE_SIZE || size > formatted.getSizeLimit()) {
            return false;
        }

        List<Pattern> includes = formatted.getIncludeFilenames();
        for (Pattern include : includes) {
            if (include.matcher(filename).find()) {
                return true;
            }
        }

        String ext = StrUtil.emptyToDefault(FileUtil.extName(file), file.getName());
        Boolean index = indexCache.get(ext);
        if (Objects.isNull(index)) {
            index = false;
            try {
                index = FileTypeUtils.isTextFile(file.getAbsolutePath());
            } catch (Exception e) {
                // ignore
            }
            indexCache.put(ext, index);
        }
        return index;
    }
}
