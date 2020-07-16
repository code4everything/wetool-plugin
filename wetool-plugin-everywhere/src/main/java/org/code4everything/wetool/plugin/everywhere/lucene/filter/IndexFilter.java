package org.code4everything.wetool.plugin.everywhere.lucene.filter;

import java.io.File;

/**
 * @author pantao
 * @since 2019/11/26
 */
public interface IndexFilter {

    /**
     * 是否对该文件创建索引
     *
     * @param file 文件
     *
     * @return 是否索引
     */
    boolean shouldIndex(File file);
}
