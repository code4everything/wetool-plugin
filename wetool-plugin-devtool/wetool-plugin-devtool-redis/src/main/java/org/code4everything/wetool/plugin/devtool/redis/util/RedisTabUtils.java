package org.code4everything.wetool.plugin.devtool.redis.util;

import cn.hutool.core.lang.func.VoidFunc1;
import cn.hutool.core.util.ObjectUtil;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import lombok.experimental.UtilityClass;

import java.util.Objects;

/**
 * @author pantao
 * @since 2019/11/14
 */
@UtilityClass
public class RedisTabUtils {

    public static void openTab(TabPane tabPane, String url, String label, VoidFunc1<String> added) throws Exception {
        Node node = FXMLLoader.load(RedisTabUtils.class.getResource(url));
        Tab tab = new Tab(label);
        tab.setContent(node);
        tab.setId(label);
        tab.setClosable(true);

        for (int i = 0; i < tabPane.getTabs().size(); i++) {
            Tab t = tabPane.getTabs().get(i);
            if (Objects.equals(t.getId(), tab.getId())) {
                // 选项卡已打开
                tabPane.getSelectionModel().select(i);
                return;
            }
        }

        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tabPane.getTabs().size() - 1);

        if (ObjectUtil.isNotNull(added)) {
            added.call(tab.getText());
        }
    }
}
