package org.code4everything.wetool.plugin.devtool.redis.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import javafx.fxml.FXML;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import org.code4everything.wetool.plugin.devtool.redis.config.ConnectionConfiguration;
import org.code4everything.wetool.plugin.devtool.redis.config.RedisConfiguration;
import org.code4everything.wetool.plugin.devtool.redis.constant.CommonConsts;
import org.code4everything.wetool.plugin.devtool.redis.util.RedisTabUtils;
import org.code4everything.wetool.plugin.support.BaseViewController;
import org.code4everything.wetool.plugin.support.factory.BeanFactory;
import org.code4everything.wetool.plugin.support.util.FxDialogs;
import org.code4everything.wetool.plugin.support.util.FxUtils;
import redis.clients.jedis.Jedis;

import java.util.*;

/**
 * @author pantao
 * @since 2019/11/13
 */
public class MainController implements BaseViewController {

    private final Map<String, ConnectionConfiguration> configurationMap = new HashMap<>(8);

    private final Map<String, Jedis> connectionMap = new HashMap<>(8);

    @FXML
    public TextField currentServerDb;

    @FXML
    public TreeView<String> redisExplorer;

    @FXML
    public TabPane redisExplorerTab;

    private TreeItem<String> rootTree = new TreeItem<>("Redis Servers");

    private int currentDb;

    @FXML
    private void initialize() {
        BeanFactory.registerView(CommonConsts.APP_ID, CommonConsts.APP_NAME, this);
        redisExplorer.setRoot(rootTree);
        reloadConfig();
    }

    public void openConfigFile() {
        String path = RedisConfiguration.getPath();
        if (!FileUtil.exist(path)) {
            FileUtil.writeUtf8String("{\"servers\":[]}", path);
        }
        FxUtils.openFile(path);
    }

    public void reloadConfig() {
        configurationMap.clear();
        connectionMap.clear();
        rootTree.getChildren().clear();
        // 加载配置
        RedisConfiguration redisConfiguration = RedisConfiguration.getConfiguration();
        Set<ConnectionConfiguration> servers = redisConfiguration.getServers();
        if (CollUtil.isEmpty(servers)) {
            return;
        }
        // 解析配置
        servers.forEach(server -> {
            TreeItem<String> serverTree = new TreeItem<>(server.getAlias());
            if (CollUtil.isNotEmpty(server.getDbs())) {
                SortedSet<Integer> sortedSet = new TreeSet<>(server.getDbs());
                sortedSet.forEach(db -> {
                    TreeItem<String> dbTree = new TreeItem<>("db" + db);
                    serverTree.getChildren().add(dbTree);
                });
            }
            rootTree.getChildren().add(serverTree);
            configurationMap.put(server.getAlias(), server);
        });
    }

    public void openRedis(MouseEvent event) {
        TreeItem<String> source = redisExplorer.getSelectionModel().getSelectedItem();
        if (source == rootTree) {
            return;
        }
        FxUtils.doubleClicked(event, () -> {
            if (source.getParent() == rootTree) {
                currentServerDb.setText(source.getValue() + ":db0");
            } else {
                currentServerDb.setText(source.getParent().getValue() + ":" + source.getValue());
            }
            try {
                openTab();
            } catch (Exception e) {
                FxDialogs.showException(CommonConsts.APP_NAME, e);
            }
        });
    }

    private void openTab() throws Exception {
        String url = "/ease/devtool/redis/Explorer.fxml";
        RedisTabUtils.openTab(redisExplorerTab, url, currentServerDb.getText(), serverDb -> {
            int idx = serverDb.lastIndexOf(":");
            openConnection(serverDb.substring(0, idx), serverDb.substring(idx + 1));
        });
    }

    private void openConnection(String alias, String db) {
        //        Jedis jedis = connectionMap.get(alias);
        //        if (Objects.isNull(jedis)) {
        //            ConnectionConfiguration conf = configurationMap.get(alias);
        //            jedis = new Jedis(conf.getHost(), conf.getPort());
        //            if (StrUtil.isNotEmpty(conf.getPassword())) {
        //                jedis.auth(conf.getPassword());
        //            }
        //            connectionMap.put(alias, jedis);
        //        }
        //        if (!jedis.isConnected()) {
        //            jedis.connect();
        //        }
        currentDb = StrUtil.isEmpty(db) ? 0 : NumberUtil.parseInt(StrUtil.removePrefix(db, "db"));
    }
}
