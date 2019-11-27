package org.code4everything.wetool.plugin.everywhere.lucene;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.code4everything.wetool.plugin.everywhere.constant.CommonConsts;
import org.code4everything.wetool.plugin.everywhere.model.FileInfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author pantao
 * @since 2019/11/27
 */
public class LuceneSearcher {

    private IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(CommonConsts.INDEX_PATH)));

    private IndexSearcher searcher = new IndexSearcher(reader);

    private Analyzer analyzer = new SmartChineseAnalyzer();

    private QueryParser filepathParser = new QueryParser("filepath", analyzer);

    private QueryParser filenameParser = new QueryParser("filename", analyzer);

    private QueryParser contentParser = new QueryParser("content", analyzer);

    public LuceneSearcher() throws IOException {}

    public List<FileInfo> search(String word, boolean addFolder, boolean addFile, boolean content) throws IOException
            , ParseException {
        Predicate<File> filter = f -> (addFolder && FileUtil.isDirectory(f)) || (addFile && FileUtil.isFile(f));
        List<FileInfo> list = new LinkedList<>();
        if (addFile || addFolder) {
            convertAndFilter(filepathParser.parse(word), list, filter);
            convertAndFilter(filenameParser.parse(word), list, filter);
        }
        if (content) {
            convertAndFilter(contentParser.parse(word), list, null);
        }
        return list;
    }

    private void convertAndFilter(Query query, List<FileInfo> list, Predicate<File> filter) throws IOException {
        final TopDocs results = searcher.search(query, Integer.MAX_VALUE);
        final ScoreDoc[] hits = results.scoreDocs;

        if (ArrayUtil.isEmpty(hits)) {
            return;
        }

        for (ScoreDoc hit : hits) {
            final Document doc = searcher.doc(hit.doc);
            final String path = doc.get("path");
            if (!FileUtil.exist(path)) {
                continue;
            }
            File file = new File(path);
            if (ObjectUtil.isNotNull(filter) && !filter.test(file)) {
                continue;
            }
            String date = DateUtil.formatDateTime(FileUtil.lastModifiedTime(file));
            list.add(new FileInfo(file.getName(), file.getPath(), date, FileUtil.readableFileSize(file)));
        }
    }
}
