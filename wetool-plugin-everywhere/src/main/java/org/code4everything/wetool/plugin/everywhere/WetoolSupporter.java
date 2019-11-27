package org.code4everything.wetool.plugin.everywhere;

import cn.hutool.core.thread.ThreadUtil;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import org.code4everything.wetool.plugin.everywhere.constant.CommonConsts;
import org.code4everything.wetool.plugin.everywhere.lucene.LuceneIndexer;
import org.code4everything.wetool.plugin.support.WePluginSupporter;
import org.code4everything.wetool.plugin.support.util.FxUtils;

import java.io.IOException;

/**
 * @author pantao
 * @since 2019/11/26
 */
public class WetoolSupporter implements WePluginSupporter {

    private LuceneIndexer luceneIndexer = new LuceneIndexer();

    @Override
    public boolean initialize() {
        ThreadUtil.execute(() -> {
            try {
                luceneIndexer.createIndex();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return true;
    }

    @Override
    public void debugCall() {
        openTab();
    }

    @Override
    public MenuItem registerBarMenu() {
        return FxUtils.createMenuItem(CommonConsts.APP_NAME, (EventHandler<ActionEvent>) event -> openTab());
    }

    private void openTab() {
        Node node = FxUtils.loadFxml("/ease/everywhere/Main.fxml");
        FxUtils.openTab(node, CommonConsts.APP_ID, CommonConsts.APP_NAME);
    }
}
