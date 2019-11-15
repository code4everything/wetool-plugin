package org.code4everything.wetool.plugin.devtool.redis.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import org.code4everything.boot.base.StringUtils;
import org.code4everything.wetool.plugin.devtool.redis.jedis.JedisUtils;
import org.code4everything.wetool.plugin.devtool.redis.jedis.JedisVO;
import org.code4everything.wetool.plugin.support.util.FxUtils;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;


/**
 * @author pantao
 * @since 2019/11/13
 */
public class ExplorerController {

    @FXML
    public TabPane keyTab;

    @FXML
    public ComboBox<String> searchWay;

    @FXML
    public TextField searchText;

    @FXML
    public TableColumn<JedisVO, String> sizeColumn;

    @FXML
    public TableColumn<JedisVO, String> typeColumn;

    @FXML
    public TableColumn<JedisVO, String> keyColumn;

    @FXML
    public TableView<JedisVO> keyTable;

    private JedisUtils.RedisServer redisServer;

    @FXML
    private void initialize() {
        redisServer = JedisUtils.getRedisServer();
        searchWay.getItems().addAll("Container", "Precise", "Pattern");

        sizeColumn.setCellValueFactory(new PropertyValueFactory<>("size"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        keyColumn.setCellValueFactory(new PropertyValueFactory<>("key"));
    }

    public void doSearch() {
        if ("Container".equals(searchWay.getValue())) {
            searchForContainer();
            return;
        }
    }

    private void searchForContainer() {
        // 查找容器下的Key
        String keyParent = StrUtil.addSuffixIfNot(searchText.getText(), ":");
        String keyPattern = StrUtil.addSuffixIfNot(keyParent, "*");
        Jedis jedis = JedisUtils.getJedis(redisServer);
        Set<String> keys = new TreeSet<>(jedis.keys(keyPattern));
        keyTable.getItems().clear();

        List<JedisVO> list = new ArrayList<>(keys.size());
        keys.forEach(key -> {
            JedisVO jedisVO = new JedisVO();
            String child = StrUtil.removePrefix(key, keyParent);
            child = StringUtils.trim(child, ':', 2);
            int idx = child.indexOf(":");
            if (idx > 0 && idx < StringUtils.trim(child, ':', 1).length()) {
                // 添加容器
                jedisVO.setKey(StrUtil.removeSuffix(key, child.substring(idx)));
                jedisVO.setSize("-");
                jedisVO.setType("Container");
            } else {
                // 添加KEY
                jedisVO.setKey(key);
                jedisVO.setSize(FileUtil.readableFileSize(jedis.bitcount(key)));
                jedisVO.setType(jedis.type(key));
            }
            list.add(jedisVO);
        });
        keyTable.getItems().addAll(list);
    }

    public void search(KeyEvent keyEvent) {
        FxUtils.enterDo(keyEvent, this::doSearch);
    }
}
