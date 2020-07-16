package org.code4everything.wetool.plugin.everywhere.lucene.filter;

import cn.hutool.core.io.FileUtil;
import org.code4everything.boot.base.constant.StringConsts;
import org.code4everything.wetool.plugin.everywhere.config.EverywhereConfiguration;
import org.code4everything.wetool.plugin.everywhere.constant.CommonConsts;

import java.io.File;

/**
 * 文件夹索引过滤器
 *
 * @author pantao
 * @since 2019/11/26
 */
public class FolderFilter implements IndexFilter {

    @Override
    public boolean shouldIndex(File file) {
        if (!FileUtil.exist(file)) {
            return false;
        }
        if (file.isFile()) {
            return false;
        }
        if (file.getName().startsWith(StringConsts.Sign.DOT)) {
            return false;
        }
        final EverywhereConfiguration.Formatted formatted = EverywhereConfiguration.getFormatted();
        if (formatted.isIgnoreHiddenFile() && file.isHidden()) {
            return false;
        }
        return !CommonConsts.INDEX_PATH.equals(file.getAbsolutePath());
    }
}
