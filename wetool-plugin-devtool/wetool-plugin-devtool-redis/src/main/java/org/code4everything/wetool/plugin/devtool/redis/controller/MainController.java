package org.code4everything.wetool.plugin.devtool.redis.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.swing.clipboard.ClipboardUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import javafx.fxml.FXML;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import lombok.extern.slf4j.Slf4j;
import org.code4everything.wetool.plugin.devtool.redis.config.ConnectionConfiguration;
import org.code4everything.wetool.plugin.devtool.redis.config.RedisConfiguration;
import org.code4everything.wetool.plugin.devtool.redis.constant.CommonConsts;
import org.code4everything.wetool.plugin.devtool.redis.jedis.JedisUtils;
import org.code4everything.wetool.plugin.devtool.redis.model.RedisKeyValue;
import org.code4everything.wetool.plugin.devtool.redis.util.RedisTabUtils;
import org.code4everything.wetool.plugin.support.BaseViewController;
import org.code4everything.wetool.plugin.support.exception.ToDialogException;
import org.code4everything.wetool.plugin.support.factory.BeanFactory;
import org.code4everything.wetool.plugin.support.util.DialogWinnable;
import org.code4everything.wetool.plugin.support.util.FxDialogs;
import org.code4everything.wetool.plugin.support.util.FxUtils;
import org.code4everything.wetool.plugin.support.util.WeUtils;

import java.util.*;
import java.util.regex.Pattern;

/**
 * @author pantao
 * @since 2019/11/13
 */
@Slf4j
public class MainController implements BaseViewController {

    private final Pattern serverDbPattern = Pattern.compile(".+:db\\d+$");

    private final TreeItem<String> rootTree = new TreeItem<>("Redis Servers");

    @FXML
    public TextField currentServerDb;

    @FXML
    public TreeView<String> redisExplorer;

    @FXML
    public TabPane redisExplorerTab;

    /**
     * 右键点击的数据库
     */
    private JedisUtils.RedisServer rightClickedRedisServer;

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

        String alias = source.getParent().getValue();
        String db = source.getValue();

        if (event.getButton() == MouseButton.SECONDARY && source.getParent() != rootTree) {
            handleRightClick(alias, db);
            return;
        }

        if (event.getClickCount() != 2) {
            return;
        }

        if (source.getParent() == rootTree) {
            currentServerDb.setText(db + ":db0");
        } else {
            currentServerDb.setText(alias + ":" + db);
        }

        openTab();
    }

    /**
     * 粘贴 key value
     */
    private void handleRightClick(String alias, String db) {
        // 检查剪贴板格式
        String str = StrUtil.trim(ClipboardUtil.getStr());
        String keyValueList = StrUtil.removePrefix(str, CommonConsts.KEY_VALUE_COPY_PREFIX);
        List<RedisKeyValue> redisKeyValueList = new ArrayList<>();
        try {
            redisKeyValueList.addAll(JSON.parseArray(keyValueList, RedisKeyValue.class));
        } catch (Exception e) {
            log.debug("redis key value parse error, source: {}", str);
        }
        if (CollUtil.isEmpty(redisKeyValueList)) {
            return;
        }

        int dbInt = NumberUtil.parseInt(StrUtil.removePrefix(db, "db"));
        rightClickedRedisServer = new JedisUtils.RedisServer(alias, dbInt);
        String header = StrUtil.format("将复制的键值数据粘贴到此数据库（{}: {}）？", alias, db);
        FxDialogs.showDialog(header, null, new DialogWinnable<String>() {
            @Override
            public String convertResult() {
                return "ok";
            }

            @Override
            public void consumeResult(String result) {
                if (StrUtil.isEmpty(result)) {
                    return;
                }
                // 确定粘贴
                RedisTabUtils.loadValueControllerOnly("粘贴到数据库失败！", controller -> {
                    redisKeyValueList.forEach(keyValue -> {
                        controller.keyText.setText(keyValue.getKey());
                        controller.valueText.setText(keyValue.getValue());
                        controller.expireText.setText(String.valueOf(keyValue.getExpire()));

                        // 赋值并写入缓存
                        JedisUtils.KeyExplorer keyExplorer = new JedisUtils.KeyExplorer(rightClickedRedisServer,
                                keyValue.getKey(), keyValue.getType());
                        controller.setKeyExplorer(keyExplorer);
                        controller.setKey(keyValue.getKey());
                        controller.update(false);
                    });
                    FxDialogs.showSuccess();
                });
            }
        });
    }

    private void openTab() throws Exception {
        String url = "/ease/devtool/redis/Explorer.fxml";
        String label = currentServerDb.getText();
        RedisTabUtils.openTab(redisExplorerTab, url, label, label, () -> {
            int idx = label.lastIndexOf(':');
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
            throw ToDialogException.ofError("输入格式不正确");
        }
        int idx = curr.lastIndexOf(':');
        if (!JedisUtils.containsServer(curr.substring(0, idx))) {
            throw ToDialogException.ofError("找不到对应的连接信息，请前往配置文件添加！");
        }
        openTab();
    }
}
