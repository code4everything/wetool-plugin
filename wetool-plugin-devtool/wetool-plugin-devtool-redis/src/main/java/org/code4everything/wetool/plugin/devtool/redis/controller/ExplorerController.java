package org.code4everything.wetool.plugin.devtool.redis.controller;

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
    }

    public void doSearch() {
        keyTable.getItems().clear();
        List<JedisVO> list;
        if ("Container".equals(searchWay.getValue())) {
            list = searchForContainer();
        } else if ("Pattern".equals(searchWay.getValue())) {
            list = searchForPattern();
        } else {
            list = searchForPrecise();
        }
        list.sort(this);
        keyTable.getItems().addAll(list);
    }

    private List<JedisVO> searchForPrecise() {
        return searchFor(searchText.getText());
    }

    private List<JedisVO> searchFor(String pattern) {
        Jedis jedis = JedisUtils.getJedis(redisServer);
        Set<String> keys = jedis.keys(pattern);
        List<JedisVO> list = new ArrayList<>(keys.size());
        keys.forEach(key -> {
            JedisVO jedisVO = new JedisVO().setKey(key);
            jedisVO.setType(jedis.type(key));
            //jedisVO.setSize(FileUtil.readableFileSize(jedis.bitcount(key)));
            list.add(jedisVO);
        });
        return list;
    }

    private List<JedisVO> searchForPattern() {
        String pattern = StrUtil.addPrefixIfNot(searchText.getText(), "*");
        pattern = StrUtil.addSuffixIfNot(pattern, "*");
        return searchFor(StrUtil.emptyToDefault(pattern, "*"));
    }

    private List<JedisVO> searchForContainer() {
        // 查找容器下的Key
        String keyParent = StrUtil.addSuffixIfNot(searchText.getText(), ":");
        String keyPattern = StrUtil.addSuffixIfNot(keyParent, "*");
        Jedis jedis = JedisUtils.getJedis(redisServer);
        Set<String> keys = jedis.keys(StrUtil.emptyToDefault(keyPattern, "*"));

        Set<JedisVO> list = new HashSet<>(keys.size(), 1);
        keys.forEach(key -> {
            if (!list.contains(key)) {
                JedisVO jedisVO = new JedisVO();
                String child = StrUtil.removePrefix(key, keyParent);
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
                    //jedisVO.setSize(FileUtil.readableFileSize(jedis.bitcount(key)));
                }
                list.add(jedisVO);
            }
        });
        return new ArrayList<>(list);
    }

    public void search(KeyEvent keyEvent) {
        FxUtils.enterDo(keyEvent, this::doSearch);
    }

    @Override
    public int compare(JedisVO j1, JedisVO j2) {
        Integer o1 = keyOrder.getOrDefault(j1.getType(), 99);
        Integer o2 = keyOrder.getOrDefault(j2.getType(), 99);
        int diff = o1.compareTo(o2);
        return diff == 0 ? j1.getKey().compareTo(j2.getKey()) : diff;
    }
}
