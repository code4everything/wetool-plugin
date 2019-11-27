package org.code4everything.wetool.plugin.everywhere.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.swing.clipboard.ClipboardUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.apache.lucene.queryparser.classic.ParseException;
import org.code4everything.wetool.plugin.everywhere.config.EverywhereConfiguration;
import org.code4everything.wetool.plugin.everywhere.constant.CommonConsts;
import org.code4everything.wetool.plugin.everywhere.lucene.LuceneSearcher;
import org.code4everything.wetool.plugin.everywhere.model.FileInfo;
import org.code4everything.wetool.plugin.support.BaseViewController;
import org.code4everything.wetool.plugin.support.factory.BeanFactory;
import org.code4everything.wetool.plugin.support.util.FxDialogs;
import org.code4everything.wetool.plugin.support.util.FxUtils;
import org.code4everything.wetool.plugin.support.util.WeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author pantao
 * @since 2019/11/26
 */
public class MainController implements BaseViewController {

    private final LuceneSearcher searcher = new LuceneSearcher();

    @FXML
    public CheckBox folderCheck;

    @FXML
    public CheckBox fileCheck;

    @FXML
    public CheckBox contentCheck;

    @FXML
    public TextField searchText;

    @FXML
    public TableView<FileInfo> fileTable;

    @FXML
    public TableColumn<FileInfo, String> nameColumn;

    @FXML
    public TableColumn<FileInfo, String> pathColumn;

    @FXML
    public TableColumn<FileInfo, String> sizeColumn;

    @FXML
    public TableColumn<FileInfo, String> timeColumn;

    public MainController() throws IOException {}

    @FXML
    private void initialize() {
        BeanFactory.registerView(CommonConsts.APP_ID, CommonConsts.APP_NAME, this);
        fileTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // 设置表格列对应的属性
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("filename"));
        pathColumn.setCellValueFactory(new PropertyValueFactory<>("path"));
        sizeColumn.setCellValueFactory(new PropertyValueFactory<>("size"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("modified"));
    }

    public void openConfigFile() {
        String path = EverywhereConfiguration.getPath();
        if (!FileUtil.exist(path)) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("includeFilenames", Collections.emptySet());
            jsonObject.put("excludeFilenames", Collections.emptySet());
            jsonObject.put("ignoreHiddenFile", true);
            jsonObject.put("sizeLimit", "10,000,000");
            FileUtil.writeUtf8String(jsonObject.toJSONString(), path);
        }
        FxUtils.openFile(path);
    }

    public void reloadConfig() {
        final EverywhereConfiguration.Formatted formatted = EverywhereConfiguration.loadConfiguration();
        Console.log(formatted);
    }

    public void findEverywhere() throws IOException, ParseException {
        final String word = searchText.getText();
        if (StrUtil.isEmpty(word)) {
            return;
        }

        final boolean folder = folderCheck.isSelected();
        final boolean file = fileCheck.isSelected();
        final boolean content = contentCheck.isSelected();

        if (!folder && !file && !content) {
            return;
        }

        fileTable.getItems().clear();
        List<FileInfo> list = searcher.search(word, folder, file, content);
        if (CollUtil.isEmpty(list)) {
            FxDialogs.showInformation("糟糕，什么也没找到！", null);
        } else {
            fileTable.getItems().addAll(list);
        }
    }

    public void keyReleased(KeyEvent keyEvent) throws IOException, ParseException {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            findEverywhere();
        }
    }

    public void openFile() {
        final ObservableList<FileInfo> list = fileTable.getSelectionModel().getSelectedItems();
        if (CollUtil.isEmpty(list)) {
            return;
        }
        list.forEach(f -> FxUtils.openFile(f.getPath()));
    }

    public void openFolder() {
        final ObservableList<FileInfo> list = fileTable.getSelectionModel().getSelectedItems();
        if (CollUtil.isEmpty(list)) {
            return;
        }
        list.forEach(f -> FxUtils.openFile(WeUtils.parseFolder(FileUtil.file(f.getPath()))));
    }

    public void deleteFile() {
        final ObservableList<FileInfo> list = fileTable.getSelectionModel().getSelectedItems();
        if (CollUtil.isEmpty(list)) {
            return;
        }
        List<FileInfo> deleted = new ArrayList<>(list.size());
        list.forEach(f -> {
            if (FileUtil.del(f.getPath())) {
                deleted.add(f);
                deleted.add(f);
            }
        });
        fileTable.getItems().removeAll(deleted);
    }

    public void copyFilePath() {
        final ObservableList<FileInfo> list = fileTable.getSelectionModel().getSelectedItems();
        if (CollUtil.isEmpty(list)) {
            return;
        }
        ClipboardUtil.setStr(list.get(0).getPath());
    }
}
