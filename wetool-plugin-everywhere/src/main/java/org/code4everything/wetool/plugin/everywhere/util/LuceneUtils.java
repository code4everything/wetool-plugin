package org.code4everything.wetool.plugin.everywhere.util;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ObjectUtil;
import javafx.application.Platform;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.code4everything.wetool.plugin.everywhere.constant.CommonConsts;
import org.code4everything.wetool.plugin.everywhere.lucene.LuceneIndexer;
import org.code4everything.wetool.plugin.everywhere.lucene.LuceneSearcher;
import org.code4everything.wetool.plugin.everywhere.model.FileInfo;
import org.code4everything.wetool.plugin.support.util.Callable;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

/**
 * @author pantao
 * @since 2019/11/27
 */
@Slf4j
@UtilityClass
public class LuceneUtils {

    private static final LuceneIndexer LUCENE_INDEXER = new LuceneIndexer();

    private static final AtomicBoolean INDEXING = new AtomicBoolean(false);

    private static final AtomicBoolean SEARCHING = new AtomicBoolean(false);

    private static LuceneSearcher searcher = null;

    private static Callable<List<FileInfo>> searchNotification;

    public static void setSearchNotification(Callable<List<FileInfo>> searchNotification) {
        LuceneUtils.searchNotification = searchNotification;
    }

    public static void searchAsync(String word, boolean folder, boolean file, boolean content, Pattern filterPattern) {
        if (SEARCHING.get()) {
            return;
        }
        SEARCHING.set(true);
        ThreadUtil.execute(() -> {
            try {
                List<FileInfo> list = getSearcher().search(word, folder, file, content, filterPattern);
                if (ObjectUtil.isNotNull(searchNotification)) {
                    Platform.runLater(() -> searchNotification.call(list));
                }
            } catch (Exception e) {
                log.error("searching error: " + ExceptionUtil.stacktraceToString(e));
            }
            SEARCHING.set(false);
        });
    }

    public static void indexAsync(boolean force) {
        if (INDEXING.get()) {
            return;
        }
        INDEXING.set(true);
        ThreadUtil.execute(() -> {
            try {
                if (force) {
                    FileUtil.del(CommonConsts.INDEX_PATH);
                }
                LUCENE_INDEXER.createIndex();
            } catch (IOException e) {
                log.error("indexing file error: " + ExceptionUtil.stacktraceToString(e));
            }
            INDEXING.set(false);
        });
    }

    private static LuceneSearcher getSearcher() throws IOException {
        if (Objects.isNull(searcher)) {
            searcher = new LuceneSearcher();
        }
        return searcher;
    }
}
