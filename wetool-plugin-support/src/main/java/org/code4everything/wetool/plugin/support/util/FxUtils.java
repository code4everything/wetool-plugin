package org.code4everything.wetool.plugin.support.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.base.Preconditions;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.code4everything.boot.base.function.VoidFunction;
import org.code4everything.wetool.plugin.support.BaseViewController;
import org.code4everything.wetool.plugin.support.WePluginSupportable;
import org.code4everything.wetool.plugin.support.constant.AppConsts;
import org.code4everything.wetool.plugin.support.factory.BeanFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author pantao
 * @since 2019/7/4
 **/
@Slf4j
@UtilityClass
public class FxUtils {

    public static BaseViewController getSelectedTabController() {
        Tab tab = getTabPane().getSelectionModel().getSelectedItem();
        return Objects.isNull(tab) ? null : BeanFactory.getView(tab.getId() + tab.getText());
    }

    public static void openTab(Node tabContent, String tabName) {
        openTab(tabContent, AppConsts.Title.APP_TITLE, tabName);
    }

    /**
     * 插件打开自己的选项卡请条用此方法
     */
    public static void openTab(Node tabContent, String tabId, String tabName) {
        // 校验参数
        Preconditions.checkNotNull(tabContent, "tab content node must not null");
        Preconditions.checkArgument(StrUtil.isNotEmpty(tabId), "tab id must not empty, please set a custom unique id");
        Preconditions.checkArgument(StrUtil.isNotEmpty(tabName), "tab name must not be empty");

        Tab tab = new Tab(tabName, tabContent);
        tab.setId(tabId);
        tab.setClosable(true);

        TabPane tabPane = getTabPane();
        for (int i = 0; i < tabPane.getTabs().size(); i++) {
            Tab t = tabPane.getTabs().get(i);
            if (Objects.equals(t.getId(), tab.getId()) && Objects.equals(t.getText(), tab.getText())) {
                // 选项卡已打开
                tabPane.getSelectionModel().select(i);
                return;
            }
        }

        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tabPane.getTabs().size() - 1);
    }

    public static TabPane getTabPane() {
        return BeanFactory.get(TabPane.class);
    }

    public static Stage getStage() {
        return BeanFactory.get(Stage.class);
    }

    public static void saveFile(Callable<File> callable) {
        File file = getFileChooser().showSaveDialog(getStage());
        handleFileCallable(file, callable);
    }

    public static void chooseFiles(Callable<List<File>> callable) {
        List<File> files = getFileChooser().showOpenMultipleDialog(getStage());
        handleFileListCallable(files, callable);
    }

    public static void chooseFile(Callable<File> callable) {
        File file = getFileChooser().showOpenDialog(getStage());
        handleFileCallable(file, callable);
    }

    public static void chooseFolder(Callable<File> callable) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(AppConsts.Title.APP_TITLE);
        chooser.setInitialDirectory(new File(WeUtils.getConfig().getFileChooserInitDir()));
        File file = chooser.showDialog(getStage());
        handleFileCallable(file, callable);
    }

    public static void openLink(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (URISyntaxException | IOException e) {
            FxDialogs.showException(AppConsts.Tip.OPEN_LINK_ERROR, e);
        }
    }

    public static void openFile(String file) {
        try {
            Desktop.getDesktop().open(FileUtil.file(file));
        } catch (Exception e) {
            FxDialogs.showException(AppConsts.Tip.OPEN_FILE_ERROR, e);
        }
    }

    public static void restart() {
        // 获取当前程序运行路径
        final String jarPath = System.getProperty("java.class.path");
        // 文件名的截取索引
        final int idx = Math.max(jarPath.lastIndexOf('/'), jarPath.lastIndexOf('\\')) + 1;
        ThreadUtil.execute(() -> RuntimeUtil.execForStr("java -jar ./" + jarPath.substring(idx)));
        WeUtils.exitSystem();
    }

    public static void dropFileContent(TextInputControl control, DragEvent event) {
        dropFiles(event, files -> control.setText(FileUtil.readUtf8String(files.get(0))));
    }

    public static void dropFiles(DragEvent event, Map<Object, Callable<List<File>>> eventCallableMap) {
        handleFileListCallable(event.getDragboard().getFiles(), eventCallableMap.get(event.getSource()));
    }

    public static void dropFiles(DragEvent event, Callable<List<File>> callable) {
        handleFileListCallable(event.getDragboard().getFiles(), callable);
    }

    public static void acceptCopyMode(DragEvent event) {
        event.acceptTransferModes(TransferMode.COPY);
    }

    public static void enterDo(KeyEvent event, VoidFunction function) {
        if (event.getCode() == KeyCode.ENTER) {
            function.call();
        }
    }

    public static Pane loadFxml(String url) {
        return loadFxml(FxUtils.class.getResource(url), FxUtils.class.getClassLoader());
    }

    /**
     * 插件加载视图请调用此方法
     *
     * @since 1.0.0
     */
    public static Pane loadFxml(WePluginSupportable supportable, String url) {
        Class clazz = supportable.getClass();
        return loadFxml(clazz.getResource(url), clazz.getClassLoader());
    }

    private static Pane loadFxml(URL url, ClassLoader loader) {
        FXMLLoader.setDefaultClassLoader(loader);
        try {
            return FXMLLoader.load(url);
        } catch (Exception e) {
            FxDialogs.showException(AppConsts.Tip.FXML_ERROR, e);
            return null;
        }
    }

    private static FileChooser getFileChooser() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(AppConsts.Title.APP_TITLE);
        chooser.setInitialDirectory(new File(WeUtils.getConfig().getFileChooserInitDir()));
        return chooser;
    }

    private static void handleFileListCallable(List<File> files, Callable<List<File>> callable) {
        if (CollUtil.isEmpty(files) || Objects.isNull(callable)) {
            return;
        }
        WeUtils.getConfig().setFileChooserInitDir(files.get(0).getParent());
        callable.call(files);
    }

    private static void handleFileCallable(File file, Callable<File> callable) {
        if (Objects.isNull(file) || Objects.isNull(callable)) {
            return;
        }
        WeUtils.getConfig().setFileChooserInitDir(file.getParent());
        callable.call(file);
    }
}
