package org.code4everything.wetool.plugin.everywhere.lucene.filter;

import cn.hutool.core.io.FileUtil;

import java.io.File;

/**
 * 文件名索引过滤器
 *
 * @author pantao
 * @since 2019/11/26
 */
public class FilenameFilter implements IndexFilter {

    @Override
    public boolean shouldIndex(File file) {
        if (!FileUtil.exist(file)) {
            return false;
        }
        return !file.isDirectory();
    }
}
