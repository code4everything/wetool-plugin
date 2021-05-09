package org.code4everything.wetool.plugin.devtool.redis.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.swing.clipboard.ClipboardUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.Setter;
import org.code4everything.wetool.plugin.devtool.redis.constant.CommonConsts;
import org.code4everything.wetool.plugin.devtool.redis.jedis.JedisUtils;
import org.code4everything.wetool.plugin.devtool.redis.model.RedisKeyValue;
import org.code4everything.wetool.plugin.support.exception.ToDialogException;
import org.code4everything.wetool.plugin.support.util.FxDialogs;
import org.code4everything.wetool.plugin.support.util.FxUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

import java.util.*;

/**
 * @author pantao
 * @since 2019/11/13
 */
public class ValueController {

    private static final String LINE_SEP = "\n";

    @FXML
    public TextField expireText;

    @FXML
    public TextField keyText;

    @FXML
    public TextArea valueText;

    @FXML
    public ToggleGroup typeGroup;

    @FXML
    public Label serverLabel;

    @FXML
    public RadioButton typeHashRadio;

    @FXML
    public RadioButton typeSortedSetRadio;

    @FXML
    public RadioButton typeSetRadio;

    @FXML
    public RadioButton typeListRadio;

    @FXML
    public RadioButton typeStringRadio;

    @FXML
    public TextArea jsonFormatText;

    @Setter
    private JedisUtils.KeyExplorer keyExplorer;

    @Setter
    private String key;

    @FXML
    private void initialize() {
        keyExplorer = JedisUtils.getKeyExplorer();
        if (Objects.nonNull(keyExplorer)) {
            serverLabel.setText(StrUtil.format("服务器：{}，数据库：db{}", keyExplorer.getAlias(), keyExplorer.getDb()));
            keyText.setText(keyExplorer.getKey());
            key = keyText.getText();
            refresh();
        }
        jsonFormatText.setPromptText("prefix=[\r\ndelimiter=,\r\nsuffix=]\r\nwrapper=kv");
    }

    public void refresh() {
        Jedis jedis = JedisUtils.getJedis(keyExplorer);
        if (StrUtil.isEmpty(key) || !BooleanUtil.isTrue(jedis.exists(key))) {
            return;
        }

        expireText.setText(String.valueOf(jedis.ttl(key)));
        StringBuilder sb = new StringBuilder();
        switch (keyExplorer.getType()) {
            case "hash":
                Map<String, String> map = jedis.hgetAll(key);
                map.forEach((k, v) -> sb.append(k).append(": ").append(v).append(LINE_SEP));
                typeGroup.selectToggle(typeHashRadio);
                break;
            case "zset":
                Set<Tuple> tuples = jedis.zrangeWithScores(key, 0, -1);
                tuples.forEach(t -> sb.append(t.getElement()).append(": ").append(t.getScore()).append(LINE_SEP));
                typeGroup.selectToggle(typeSortedSetRadio);
                break;
            case "set":
                Set<String> set = jedis.smembers(key);
                set.forEach(m -> sb.append(m).append(LINE_SEP));
                typeGroup.selectToggle(typeSetRadio);
                break;
            case "list":
                List<String> list = jedis.lrange(key, 0, -1);
                list.forEach(e -> sb.append(e).append(LINE_SEP));
                typeGroup.selectToggle(typeListRadio);
                break;
            default:
                sb.append(jedis.get(key));
                typeGroup.selectToggle(typeStringRadio);
                break;
        }
        valueText.setText(sb.toString());
    }

    public void format2Json() {
        if (typeStringRadio.isSelected()) {
            throw ToDialogException.ofInfo("该类型无法转换为JSON！");
        }
        // 分割所有参数
        List<String> list = StrUtil.splitTrim(jsonFormatText.getText(), '\n');
        Map<String, String> template = new HashMap<>(4, 1);
        list.forEach(str -> {
            // 分割KeyValue
            List<String> items = StrUtil.splitTrim(str, '=');
            if (items.size() >= 2) {
                template.put(items.get(0), items.get(1));
            }
        });
        // 获取并设置默认值
        String prefix = template.getOrDefault("prefix", "[");
        String delimiter = template.getOrDefault("delimiter", ",");
        String suffix = template.getOrDefault("suffix", "]");
        String wrapper = template.getOrDefault("wrapper", "kv");
        StringJoiner joiner = new StringJoiner(delimiter, prefix, suffix);

        // 格式化
        List<String> values = StrUtil.splitTrim(valueText.getText(), '\n');
        boolean hasKey = typeSortedSetRadio.isSelected() || typeHashRadio.isSelected();
        values.forEach(value -> {
            String jsonKey = "";
            if (hasKey) {
                int idx = value.indexOf(':');
                jsonKey = StrUtil.sub(value, 0, idx);
                value = idx > 0 ? StrUtil.sub(value, idx + 1, value.length()) : "null";
            }
            StringBuilder sb = new StringBuilder();
            for (char c : wrapper.toCharArray()) {
                if (c == 'k') {
                    sb.append(jsonKey);
                } else if (c == 'v') {
                    sb.append(value);
                } else {
                    sb.append(c);
                }
            }
            joiner.add(sb.toString());
        });

        // 保存文件
        FxUtils.saveFile(file -> FileUtil.writeUtf8String(joiner.toString(), file));
    }

    public void update() {
        update(true);
    }

    public void update(boolean successTip) {
        if (StrUtil.isEmpty(keyText.getText())) {
            throw ToDialogException.ofInfo("Key不能为空！");
        }

        if (StrUtil.isEmpty(valueText.getText())) {
            throw ToDialogException.ofInfo("Value不能为空！");
        }

        int expire = -1;
        if (NumberUtil.isNumber(expireText.getText())) {
            expire = NumberUtil.parseInt(expireText.getText());
        } else {
            try {
                DateTime dateTime = DateUtil.parseDateTime(expireText.getText());
                expire = (int) (dateTime.getTime() / 1000);
                expireText.setText(String.valueOf(expire));
            } catch (Exception e) {
                // ignore
            }
        }

        Jedis jedis = JedisUtils.getJedis(keyExplorer);
        deleteKey();
        key = keyText.getText();

        boolean result = updateHash(jedis);
        result |= updateSortedSet(jedis);
        result |= updateSet(jedis);
        result |= updateList(jedis);
        result |= updateString(jedis);

        if (expire > 0) {
            jedis.expire(key, expire);
        }
        if (successTip && result) {
            FxDialogs.showInformation("保存成功", null);
        }
    }

    public void copyKeyValue() {
        String json = JSON.toJSONString(List.of(getRedisKeyValue()));
        ClipboardUtil.setStr(CommonConsts.KEY_VALUE_COPY_PREFIX + json);
    }

    public RedisKeyValue getRedisKeyValue() {
        RedisKeyValue redisKeyValue = new RedisKeyValue();
        redisKeyValue.setKey(keyText.getText());
        redisKeyValue.setValue(valueText.getText());
        redisKeyValue.setExpire(NumberUtil.parseInt(expireText.getText()));

        if (typeHashRadio.isSelected()) {
            redisKeyValue.setType("hash");
        } else if (typeSortedSetRadio.isSelected()) {
            redisKeyValue.setType("zset");
        } else if (typeSetRadio.isSelected()) {
            redisKeyValue.setType("set");
        } else if (typeListRadio.isSelected()) {
            redisKeyValue.setType("list");
        } else {
            redisKeyValue.setType("string");
        }

        return redisKeyValue;
    }

    private boolean updateString(Jedis jedis) {
        if (typeStringRadio.isSelected()) {
            jedis.set(key, valueText.getText());
            return true;
        }
        return false;
    }

    private boolean updateList(Jedis jedis) {
        if (typeListRadio.isSelected()) {
            List<String> list = StrUtil.splitTrim(valueText.getText(), LINE_SEP);
            jedis.rpush(key, list.toArray(new String[0]));
            return true;
        }
        return false;
    }

    private boolean updateSet(Jedis jedis) {
        if (typeSetRadio.isSelected()) {
            List<String> list = StrUtil.splitTrim(valueText.getText(), LINE_SEP);
            jedis.sadd(key, list.toArray(new String[0]));
            return true;
        }
        return false;
    }

    private boolean updateSortedSet(Jedis jedis) {
        if (typeSortedSetRadio.isSelected()) {
            List<String> list = StrUtil.splitTrim(valueText.getText(), LINE_SEP);
            Map<String, Double> map = new HashMap<>(list.size(), 1);
            for (String kv : list) {
                Pair<String, String> pair = parseKeyValue(kv);
                try {
                    double score = Double.parseDouble(Objects.requireNonNull(pair).getValue());
                    map.put(pair.getKey(), score);
                } catch (Exception e) {
                    FxDialogs.showInformation("格式不正确！", null);
                    return false;
                }
                jedis.zadd(key, map);
            }
            return true;
        }
        return false;
    }

    private boolean updateHash(Jedis jedis) {
        if (typeHashRadio.isSelected()) {
            List<String> list = StrUtil.splitTrim(valueText.getText(), LINE_SEP);
            Map<String, String> map = new HashMap<>(list.size(), 1);
            for (String kv : list) {
                Pair<String, String> pair = parseKeyValue(kv);
                if (Objects.isNull(pair)) {
                    FxDialogs.showInformation("格式不正确！", null);
                    return false;
                }
                map.put(pair.getKey(), pair.getValue());
            }
            jedis.hmset(key, map);
            return true;
        }
        return false;
    }

    private Pair<String, String> parseKeyValue(String kv) {
        if (StrUtil.isBlank(kv)) {
            return null;
        }
        int idx = kv.lastIndexOf(':');

        if (idx < 1) {
            return null;
        }

        return new Pair<>(StrUtil.trim(kv.substring(0, idx)), StrUtil.trim(kv.substring(idx + 1)));
    }

    public void delete() {
        deleteKey();
        FxDialogs.showInformation("删除成功", null);
    }

    private void deleteKey() {
        if (StrUtil.isEmpty(key)) {
            return;
        }
        JedisUtils.getJedis(keyExplorer).del(key);
    }

    public void maximize() {
        String text = valueText.getText();
        try {
            Object object = JSON.parse(text);
            text = JSON.toJSONString(object, true);
        } catch (Exception e) {
            // ignore
        }
        FxDialogs.showTextAreaDialog(keyText.getText(), text);
    }
}
