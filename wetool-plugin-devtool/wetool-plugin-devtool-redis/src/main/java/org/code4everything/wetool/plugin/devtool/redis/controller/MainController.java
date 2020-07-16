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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import org.code4everything.wetool.plugin.devtool.redis.config.ConnectionConfiguration;
import org.code4everything.wetool.plugin.devtool.redis.config.RedisConfiguration;
import org.code4everything.wetool.plugin.devtool.redis.constant.CommonConsts;
import org.code4everything.wetool.plugin.devtool.redis.jedis.JedisUtils;
import org.code4everything.wetool.plugin.devtool.redis.util.RedisTabUtils;
import org.code4everything.wetool.plugin.support.BaseViewController;
import org.code4everything.wetool.plugin.support.factory.BeanFactory;
import org.code4everything.wetool.plugin.support.util.FxDialogs;
import org.code4everything.wetool.plugin.support.util.FxUtils;

import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * @author pantao
 * @since 2019/11/13
 */
public class MainController implements BaseViewController {

    private final Pattern serverDbPattern = Pattern.compile(".+:db\\d+$");

    @FXML
    public TextField currentServerDb;

    @FXML
    public TreeView<String> redisExplorer;

    @FXML
    public TabPane redisExplorerTab;

    private TreeItem<String> rootTree = new TreeItem<>("Redis Servers");

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
        JedisUtils.clearRedis();
        rootTree.getChildren().clear();
        redisExplorerTab.getTabs().clear();
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
            JedisUtils.putRedisConf(server.getAlias(), server);
        });
    }

    public void openRedis(MouseEvent event) throws Exception {
        TreeItem<String> source = redisExplorer.getSelectionModel().getSelectedItem();
        if (Objects.isNull(source) || source == rootTree) {
            return;
        }
        if (event.getClickCount() != 2) {
            return;
        }
        if (source.getParent() == rootTree) {
            currentServerDb.setText(source.getValue() + ":db0");
        } else {
            currentServerDb.setText(source.getParent().getValue() + ":" + source.getValue());
        }
        openTab();
    }

    private void openTab() throws Exception {
        String url = "/ease/devtool/redis/Explorer.fxml";
        String label = currentServerDb.getText();
        RedisTabUtils.openTab(redisExplorerTab, url, label, label, () -> {
            int idx = label.lastIndexOf(":");
            String alias = label.substring(0, idx);
            String db = label.substring(idx + 1);
            int currentDb = StrUtil.isEmpty(db) ? 0 : NumberUtil.parseInt(StrUtil.removePrefix(db, "db"));
            JedisUtils.offerRedisServer(alias, currentDb);
        });
    }

    public void changeServerDb(KeyEvent keyEvent) throws Exception {
        if (keyEvent.getCode() != KeyCode.ENTER) {
            return;
        }
        String curr = currentServerDb.getText();
        if (!serverDbPattern.matcher(curr).matches()) {
            FxDialogs.showError("输入格式不正确！");
            return;
        }
        int idx = curr.lastIndexOf(":");
        if (!JedisUtils.containsServer(curr.substring(0, idx))) {
            FxDialogs.showError("找不到对应的连接信息，请前往配置文件添加！");
            return;
        }
        openTab();
    }
}
