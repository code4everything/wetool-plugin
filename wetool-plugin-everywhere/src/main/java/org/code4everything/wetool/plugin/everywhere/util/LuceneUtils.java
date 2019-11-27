package org.code4everything.wetool.plugin.everywhere.util;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.thread.ThreadUtil;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.code4everything.wetool.plugin.everywhere.lucene.LuceneIndexer;

import java.io.IOException;

/**
 * @author pantao
 * @since 2019/11/27
 */
@Slf4j
@UtilityClass
public class LuceneUtils {

    private static final LuceneIndexer LUCENE_INDEXER = new LuceneIndexer();

    private static boolean indexing = false;

    public static void indexAsync() {
        if (indexing) {
            return;
        }
        indexing = true;
        ThreadUtil.execute(() -> {
            try {
                LUCENE_INDEXER.createIndex();
                indexing = false;
            } catch (IOException e) {
                log.error("indexing file error: " + ExceptionUtil.stacktraceToString(e));
                indexing = false;
            }
        });
    }
}
