package org.code4everything.wetool.plugin.everywhere.lucene;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.code4everything.boot.config.BootConfig;
import org.code4everything.wetool.plugin.everywhere.config.EverywhereConfiguration;
import org.code4everything.wetool.plugin.everywhere.constant.CommonConsts;
import org.code4everything.wetool.plugin.everywhere.lucene.filter.ContentFilter;
import org.code4everything.wetool.plugin.everywhere.lucene.filter.FilenameFilter;
import org.code4everything.wetool.plugin.everywhere.lucene.filter.FolderFilter;
import org.code4everything.wetool.plugin.everywhere.lucene.filter.IndexFilter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * @author pantao
 * @since 2019/11/27
 */
@Slf4j
public class LuceneIndexer {

    private String indexedFile = StrUtil.join(File.separator, CommonConsts.INDEX_PATH, "indexed.file");

    private IndexFilter folderFilter = new FolderFilter();

    private IndexFilter filenameFilter = new FilenameFilter();

    private IndexFilter contentFilter = new ContentFilter();

    public void createIndex() throws IOException {
        if (isIndexedValid()) {
            return;
        }
        FileUtil.del(indexedFile);
        FileUtil.touch(indexedFile);
        long start = System.currentTimeMillis();
        // 配置索引方式
        @Cleanup Directory dir = FSDirectory.open(Paths.get(CommonConsts.INDEX_PATH));
        Analyzer analyzer = new SmartChineseAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        @Cleanup IndexWriter writer = new IndexWriter(dir, config);
        // 索引所有文件
        final File[] roots = File.listRoots();
        if (ArrayUtil.isNotEmpty(roots)) {
            for (File root : roots) {
                if (FileUtil.isDirectory(root)) {
                    final File[] files = FileUtil.ls(root.getAbsolutePath());
                    if (ArrayUtil.isNotEmpty(files)) {
                        for (File file : files) {
                            recursiveIndex(file, writer);
                        }
                    }
                }
            }
        }
        log.info("lucene index expend: {}ms", System.currentTimeMillis() - start);
    }

    private boolean isIndexedValid() {
        if (!FileUtil.exist(indexedFile)) {
            return false;
        }
        // 差值，毫秒转分钟
        long diff = (System.currentTimeMillis() - FileUtil.lastModifiedTime(indexedFile).getTime()) / (1000 * 60);
        EverywhereConfiguration.Formatted formatted = EverywhereConfiguration.getFormatted();
        return diff < formatted.getIndexExpire();
    }

    private void recursiveIndex(File file, IndexWriter writer) throws IOException {
        if (!FileUtil.exist(file)) {
            return;
        }
        if (filenameFilter.shouldIndex(file)) {
            indexDocument(writer, file);
        }
        if (folderFilter.shouldIndex(file)) {
            indexDocument(writer, file);
            final File[] files = FileUtil.ls(file.getAbsolutePath());
            if (ArrayUtil.isEmpty(files)) {
                return;
            }
            for (File f : files) {
                recursiveIndex(f, writer);
            }
        }
    }

    private void indexDocument(IndexWriter writer, File file) throws IOException {
        if (!FileUtil.exist(file)) {
            return;
        }
        Document document = new Document();
        // 索引标识
        document.add(new StringField("path", FileUtil.getAbsolutePath(file), Field.Store.YES));

        // 索引内容
        document.add(new TextField("filepath", FileUtil.getAbsolutePath(file), Field.Store.YES));
        if (contentFilter.shouldIndex(file)) {
            try {
                document.add(new TextField("content", FileUtil.readUtf8String(file), Field.Store.NO));
                final String log = StrUtil.format("lucene indexed file: {}\r\n", FileUtil.getAbsolutePath(file));
                FileUtil.appendString(log, indexedFile, CharsetUtil.CHARSET_UTF_8);
            } catch (Exception e) {
                // ignore
                EverywhereConfiguration.getFormatted().addExcludeFilenames(file);
            }
        }
        if (BootConfig.isDebug()) {
            Console.log("lucene indexing file: " + FileUtil.getAbsolutePath(file));
        }
        writer.updateDocument(new Term("path", FileUtil.getAbsolutePath(file)), document);
    }
}
