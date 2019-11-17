package org.code4everything.wetool.plugin.devtool.redis.util;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import lombok.experimental.UtilityClass;
import org.code4everything.wetool.plugin.devtool.redis.jedis.JedisUtils;

import java.util.Objects;

/**
 * @author pantao
 * @since 2019/11/14
 */
@UtilityClass
public class RedisTabUtils {

    public static void openTab(TabPane tabPane, String url, String label) throws Exception {
        // 是否已经打开
        for (int i = 0; i < tabPane.getTabs().size(); i++) {
            Tab t = tabPane.getTabs().get(i);
            if (Objects.equals(t.getId(), label)) {
                // 选项卡已打开
                tabPane.getSelectionModel().select(i);
                return;
            }
        }

        // 连接Redis
        int idx = label.lastIndexOf(":");
        String alias = label.substring(0, idx);
        String db = label.substring(idx + 1);
        int currentDb = StrUtil.isEmpty(db) ? 0 : NumberUtil.parseInt(StrUtil.removePrefix(db, "db"));
        JedisUtils.offerRedisServer(alias, currentDb);

        // 打开视图
        Node node = FXMLLoader.load(RedisTabUtils.class.getResource(url));
        Tab tab = new Tab(label);
        tab.setContent(node);
        tab.setId(label);
        tab.setClosable(true);

        // 选中TAB
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tabPane.getTabs().size() - 1);
    }
}
