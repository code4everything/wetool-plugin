package org.code4everything.wetool.plugin.everywhere;

import org.code4everything.wetool.plugin.everywhere.lucene.LuceneIndexer;
import org.code4everything.wetool.plugin.support.WePluginSupporter;

import java.io.IOException;

/**
 * @author pantao
 * @since 2019/11/26
 */
public class WetoolSupporter implements WePluginSupporter {

    private LuceneIndexer luceneIndexer = new LuceneIndexer();

    @Override
    public boolean initialize() {
        try {
            luceneIndexer.createIndex();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
}
