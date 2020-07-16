package org.code4everything.wetool.plugin.devtool.redis.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import org.code4everything.boot.base.StringUtils;
import org.code4everything.wetool.plugin.devtool.redis.jedis.JedisUtils;
import org.code4everything.wetool.plugin.devtool.redis.jedis.JedisVO;
import org.code4everything.wetool.plugin.devtool.redis.util.RedisTabUtils;
import org.code4everything.wetool.plugin.support.util.FxDialogs;
import redis.clients.jedis.Jedis;

import java.util.*;


/**
 * @author pantao
 * @since 2019/11/13
 */
public class ExplorerController implements Comparator<JedisVO> {

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

    private Map<String, Integer> keyOrder = new HashMap<>();

    private JedisUtils.RedisServer redisServer;

    {
        // 初始化KEY顺序
        keyOrder.put("container", 1);
        keyOrder.put("string", 2);
        keyOrder.put("list", 3);
        keyOrder.put("set", 4);
        keyOrder.put("zset", 5);
        keyOrder.put("hash", 6);
    }

    @FXML
    private void initialize() {
        redisServer = JedisUtils.getRedisServer();
        searchWay.getItems().addAll("Container", "Precise", "Pattern");
        searchWay.getSelectionModel().select(0);

        // 定义表格类型
        sizeColumn.setCellValueFactory(new PropertyValueFactory<>("size"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        keyColumn.setCellValueFactory(new PropertyValueFactory<>("key"));
        keyTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    public void doSearch() throws Exception {
        keyTable.getItems().clear();
        List<JedisVO> list = null;
        if ("Container".equals(searchWay.getValue())) {
            list = searchForContainer();
        } else if ("Pattern".equals(searchWay.getValue())) {
            list = searchForPattern();
        }
        if (CollUtil.isEmpty(list)) {
            openKeyDetail(searchText.getText(), null);
        } else {
            list.sort(this);
            keyTable.getItems().addAll(list);
        }
    }

    public void addKey() throws Exception {
        openKeyDetail(null, null);
    }

    public void search(KeyEvent keyEvent) throws Exception {
        if (keyEvent.getCode() != KeyCode.ENTER) {
            return;
        }
        doSearch();
    }

    @Override
    public int compare(JedisVO j1, JedisVO j2) {
        Integer o1 = keyOrder.getOrDefault(j1.getType(), 99);
        Integer o2 = keyOrder.getOrDefault(j2.getType(), 99);
        int diff = o1.compareTo(o2);
        return diff == 0 ? j1.getKey().compareTo(j2.getKey()) : diff;
    }

    public void tableClicked(MouseEvent mouseEvent) throws Exception {
        if (mouseEvent.getClickCount() != 2) {
            return;
        }
        JedisVO jedisVO = keyTable.getSelectionModel().getSelectedItem();
        if (jedisVO.isContainer()) {
            searchWay.setValue("Container");
            searchText.setText(jedisVO.getKey());
            doSearch();
        } else {
            openKeyDetail(jedisVO.getKey(), jedisVO.getType());
        }
    }

    private void openKeyDetail(String key, String type) throws Exception {
        String url = "/ease/devtool/redis/Value.fxml";
        String id = StrUtil.join(":", redisServer.getAlias(), redisServer.getDb(), key);
        RedisTabUtils.openTab(keyTab, url, id, key, () -> JedisUtils.offerKeyExplorer(redisServer, key, type));
    }

    private List<JedisVO> searchForPattern() {
        String pattern = StrUtil.addPrefixIfNot(searchText.getText(), "*");
        pattern = StrUtil.addSuffixIfNot(pattern, "*");
        pattern = StrUtil.emptyToDefault(pattern, "*");

        Jedis jedis = JedisUtils.getJedis(redisServer);
        Set<String> keys = jedis.keys(pattern);
        List<JedisVO> list = new ArrayList<>(keys.size());
        keys.forEach(key -> {
            JedisVO jedisVO = new JedisVO().setKey(key);
            jedisVO.setType(jedis.type(key));
            jedisVO.setSize(getSize(jedis, key, jedisVO.getType()));
            list.add(jedisVO);
        });
        return list;
    }

    private List<JedisVO> searchForContainer() {
        Set<String> keys = listKeysForContainer(searchText.getText());
        Jedis jedis = JedisUtils.getJedis(redisServer);

        Set<JedisVO> list = new HashSet<>(keys.size(), 1);
        keys.forEach(key -> {
            if (!list.contains(key)) {
                JedisVO jedisVO = new JedisVO();
                String child = StrUtil.removePrefix(key, searchText.getText());
                child = StringUtils.trim(child, ':', 2);
                int idx = child.indexOf(":");
                if (idx > 0 && idx < StringUtils.trim(child, ':', 1).length()) {
                    // 添加容器
                    jedisVO.setKey(StrUtil.removeSuffix(key, child.substring(idx)));
                    jedisVO.setSize("-");
                    jedisVO.setType("container");
                } else {
                    // 添加KEY
                    jedisVO.setKey(key);
                    jedisVO.setType(jedis.type(key));
                    jedisVO.setSize(getSize(jedis, key, jedisVO.getType()));
                }
                list.add(jedisVO);
            }
        });
        return new ArrayList<>(list);
    }

    private String getSize(Jedis jedis, String key, String type) {
        switch (type) {
            case "string":
                return FileUtil.readableFileSize(jedis.strlen(key));
            case "list":
                return String.valueOf(jedis.llen(key));
            case "set":
                return String.valueOf(jedis.smembers(key).size());
            case "zset":
                return String.valueOf(jedis.zcount(key, Double.MIN_VALUE, Double.MAX_VALUE));
            case "hash":
                return String.valueOf(jedis.hlen(key));
            default:
                return "-";
        }
    }

    public void deleteKeys() {
        Set<String> keys = new HashSet<>();
        ObservableList<JedisVO> list = keyTable.getSelectionModel().getSelectedItems();
        list.forEach(jedisVO -> {
            if (jedisVO.isContainer()) {
                keys.addAll(listKeysForContainer(jedisVO.getKey()));
            } else {
                keys.add(jedisVO.getKey());
            }
        });
        Jedis jedis = JedisUtils.getJedis(redisServer);
        jedis.del(keys.toArray(new String[0]));
        keyTable.getItems().removeAll(list);
        FxDialogs.showInformation("删除成功！", null);
    }

    private Set<String> listKeysForContainer(String container) {
        String keyParent = StrUtil.addSuffixIfNot(container, ":");
        String keyPattern = StrUtil.addSuffixIfNot(keyParent, "*");
        Jedis jedis = JedisUtils.getJedis(redisServer);
        return jedis.keys(StrUtil.emptyToDefault(keyPattern, "*"));
    }
}
