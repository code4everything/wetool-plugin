package org.code4everything.wetool.plugin.devtool.redis.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.swing.clipboard.ClipboardUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.code4everything.boot.base.StringUtils;
import org.code4everything.wetool.plugin.devtool.redis.config.GeoAmapConfiguration;
import org.code4everything.wetool.plugin.devtool.redis.config.RedisConfiguration;
import org.code4everything.wetool.plugin.devtool.redis.constant.CommonConsts;
import org.code4everything.wetool.plugin.devtool.redis.jedis.JedisUtils;
import org.code4everything.wetool.plugin.devtool.redis.jedis.JedisVO;
import org.code4everything.wetool.plugin.devtool.redis.model.RedisKeyValue;
import org.code4everything.wetool.plugin.devtool.redis.util.RedisTabUtils;
import org.code4everything.wetool.plugin.support.util.FxDialogs;
import redis.clients.jedis.GeoRadiusResponse;
import redis.clients.jedis.GeoUnit;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.GeoRadiusParam;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author pantao
 * @since 2019/11/13
 */
public class ExplorerController implements Comparator<JedisVO> {

    private static final Map<String, Integer> KEY_ORDER = new HashMap<>();

    private static final String TYPE_CONTAINER = "Container";

    static {
        // 初始化KEY顺序
        KEY_ORDER.put("container", 1);
        KEY_ORDER.put("string", 2);
        KEY_ORDER.put("list", 3);
        KEY_ORDER.put("set", 4);
        KEY_ORDER.put("zset", 5);
        KEY_ORDER.put("hash", 6);
    }

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
        searchWay.getItems().addAll(TYPE_CONTAINER, "Precise", "Pattern");
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
        if (TYPE_CONTAINER.equals(searchWay.getValue())) {
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
        Integer o1 = KEY_ORDER.getOrDefault(j1.getType(), 99);
        Integer o2 = KEY_ORDER.getOrDefault(j2.getType(), 99);
        int diff = o1.compareTo(o2);
        return diff == 0 ? j1.getKey().compareTo(j2.getKey()) : diff;
    }

    public void tableClicked(MouseEvent mouseEvent) throws Exception {
        if (mouseEvent.getClickCount() != 2) {
            return;
        }
        JedisVO jedisVO = keyTable.getSelectionModel().getSelectedItem();
        if (jedisVO.isContainer()) {
            searchWay.setValue(TYPE_CONTAINER);
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
            JedisVO jedisVO = new JedisVO();
            String child = StrUtil.removePrefix(key, searchText.getText());
            child = StringUtils.trim(child, ':', 2);
            int idx = child.indexOf(':');
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

    public void openGeo() {
        List<JedisVO> list = keyTable.getSelectionModel().getSelectedItems();

        if (CollUtil.isNotEmpty(list)) {
            list = list.stream().filter(jedisVO -> "zset".equals(jedisVO.getType())).collect(Collectors.toList());
        }
        if (CollUtil.isEmpty(list)) {
            FxDialogs.showInformation("GEO", "非法的数据结构");
            return;
        }

        JedisVO jedisVO = list.get(0);
        String geoHtml = null;
        try {
            geoHtml = getGeoMapHtml(jedisVO.getKey());
        } catch (Exception e) {
            FxDialogs.showError("非GEO结构，无法显示！");
        }

        WebView browser = new WebView();
        WebEngine webEngine = browser.getEngine();
        webEngine.loadContent(geoHtml);

        FxDialogs.showDialog(jedisVO.getKey(), browser);
    }

    public void deleteKeys() {
        ObservableList<JedisVO> list = keyTable.getSelectionModel().getSelectedItems();
        Set<String> keys = new HashSet<>();
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

    /**
     * 暂时不支持复制container
     */
    public void copyKeyValue() {
        ObservableList<JedisVO> list = keyTable.getSelectionModel().getSelectedItems();
        RedisTabUtils.loadValueControllerOnly("复制失败！", controller -> {
            List<RedisKeyValue> keyValueList = list.stream().map(jedisVO -> {
                controller.keyText.setText(jedisVO.getKey());
                controller.setKey(jedisVO.getKey());
                JedisUtils.KeyExplorer keyExplorer = new JedisUtils.KeyExplorer(redisServer, jedisVO.getKey(),
                        jedisVO.getType());
                controller.setKeyExplorer(keyExplorer);
                controller.refresh();
                return controller.getRedisKeyValue();
            }).collect(Collectors.toList());

            String json = CommonConsts.KEY_VALUE_COPY_PREFIX + JSON.toJSONString(keyValueList);
            ClipboardUtil.setStr(json);
            FxDialogs.showSuccess();
        });
    }

    private String getGeoMapHtml(String geoKey) {
        Jedis jedis = JedisUtils.getJedis(redisServer);
        GeoRadiusParam param = GeoRadiusParam.geoRadiusParam().withCoord();
        List<GeoRadiusResponse> list = jedis.georadius(geoKey, 0, 0, Double.MAX_VALUE, GeoUnit.KM, param);

        if (Objects.isNull(list)) {
            list = Collections.emptyList();
        }

        String template = "map.add(new AMap.Marker({position:[{},{}],title:'{}'}));";
        GeoAmapConfiguration amapConf = RedisConfiguration.getConfiguration().getGeoAmapConf();
        if (BooleanUtil.isTrue(amapConf.getCircleEnabled())) {
            // @formatter:off
            String circle = "map.add(new AMap.Circle({center:new AMap.LngLat({},{}),radius:{radius}" +
                    ",strokeColor:'{strokeColor}',strokeOpacity:{strokeOpacity},strokeWeight:{strokeWeight}" +
                    ",fillColor:'{fillColor}',fillOpacity:{fillOpacity}}));";
            // @formatter:on
            template += StrUtil.format(circle, BeanUtil.beanToMap(amapConf));
        }
        StringBuilder scripts = new StringBuilder().append("<!DOCTYPE html><html lang='cn'>");
        scripts.append("<head><meta charset='utf-8'><title>GEO地图</title><script type='text/javascript' ");
        scripts.append("src='https://webapi.amap.com/maps?v=1.4.15&key=f99090997c9043f8977a58e4aa2cfd5d'></script>");
        scripts.append("</head><body><div id='container' style='width:100%;height:100%;position:absolute;'></div>");
        scripts.append("<script>var map=new AMap.Map('container',{zoom:11,resizeEnable:true,viewMode:'2D'});");

        for (GeoRadiusResponse response : list) {
            double lng = response.getCoordinate().getLongitude();
            double lat = response.getCoordinate().getLatitude();
            String title = new String(response.getMember(), CharsetUtil.CHARSET_UTF_8);
            scripts.append(StrUtil.format(template, lng, lat, title, lng, lat));
        }
        return scripts.append("</script></body></html>").toString();
    }

    private Set<String> listKeysForContainer(String container) {
        String keyParent = StrUtil.addSuffixIfNot(container, ":");
        String keyPattern = StrUtil.addSuffixIfNot(keyParent, "*");
        Jedis jedis = JedisUtils.getJedis(redisServer);
        return jedis.keys(StrUtil.emptyToDefault(keyPattern, "*"));
    }
}
