package org.code4everything.wetool.plugin.devtool.redis.util;

import cn.hutool.core.util.ObjectUtil;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import lombok.experimental.UtilityClass;
import org.code4everything.boot.base.function.VoidFunction;
import org.code4everything.wetool.plugin.devtool.redis.controller.MainController;
import org.code4everything.wetool.plugin.devtool.redis.controller.ValueController;
import org.code4everything.wetool.plugin.support.util.FxDialogs;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author pantao
 * @since 2019/11/14
 */
@UtilityClass
public class RedisTabUtils {

    public static void openTab(TabPane tabPane, String url, String id, String label, VoidFunction beforeOpen) throws Exception {
        // 是否已经打开
        for (int i = 0; i < tabPane.getTabs().size(); i++) {
            Tab t = tabPane.getTabs().get(i);
            if (Objects.equals(t.getId(), id)) {
                // 选项卡已打开
                tabPane.getSelectionModel().select(i);
                return;
            }
        }

        if (ObjectUtil.isNotNull(beforeOpen)) {
            beforeOpen.call();
        }

        // 打开视图
        Node node = FXMLLoader.load(RedisTabUtils.class.getResource(url));
        Tab tab = new Tab(label);
        tab.setContent(node);
        tab.setId(id);
        tab.setClosable(true);

        // 选中TAB
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tabPane.getTabs().size() - 1);
    }

    public static void loadValueControllerOnly(String errMsg, Consumer<ValueController> consumer) {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(MainController.class.getResource("/ease/devtool/redis/Value.fxml"));
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            //            FxDialogs.showError(errMsg);
            FxDialogs.showException(errMsg, e);
            return;
        }
        ValueController controller = fxmlLoader.getController();
        consumer.accept(controller);
    }
}
