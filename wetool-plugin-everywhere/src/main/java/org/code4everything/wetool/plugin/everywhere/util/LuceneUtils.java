package org.code4everything.wetool.plugin.everywhere.util;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ObjectUtil;
import javafx.application.Platform;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.code4everything.wetool.plugin.everywhere.lucene.LuceneIndexer;
import org.code4everything.wetool.plugin.everywhere.lucene.LuceneSearcher;
import org.code4everything.wetool.plugin.everywhere.model.FileInfo;
import org.code4everything.wetool.plugin.support.util.Callable;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * @author pantao
 * @since 2019/11/27
 */
@Slf4j
@UtilityClass
public class LuceneUtils {

    private static final LuceneIndexer LUCENE_INDEXER = new LuceneIndexer();

    private static boolean indexing = false;

    private static boolean searching = false;

    private static LuceneSearcher searcher = null;

    private static Callable<List<FileInfo>> searchNotification;

    public static void setSearchNotification(Callable<List<FileInfo>> searchNotification) {
        LuceneUtils.searchNotification = searchNotification;
    }

    public static LuceneIndexer getLuceneIndexer() {
        return LUCENE_INDEXER;
    }

    public static void searchAsync(String word, boolean folder, boolean file, boolean content, Pattern filterPattern) {
        if (searching) {
            return;
        }
        searching = true;
        ThreadUtil.execute(() -> {
            try {
                List<FileInfo> list = getSearcher().search(word, folder, file, content, filterPattern);
                searching = false;
                if (ObjectUtil.isNotNull(searchNotification)) {
                    Platform.runLater(() -> searchNotification.call(list));
                }
            } catch (Exception e) {
                searching = false;
                log.error("searching error: " + ExceptionUtil.stacktraceToString(e));
            }
        });
    }

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

    private static LuceneSearcher getSearcher() throws IOException {
        if (Objects.isNull(searcher)) {
            searcher = new LuceneSearcher();
        }
        return searcher;
    }
}
