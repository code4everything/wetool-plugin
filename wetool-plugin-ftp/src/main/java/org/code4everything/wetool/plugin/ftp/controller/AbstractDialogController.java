package org.code4everything.wetool.plugin.ftp.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyEvent;
import org.code4everything.boot.base.constant.StringConsts;
import org.code4everything.wetool.plugin.ftp.FtpManager;
import org.code4everything.wetool.plugin.ftp.constant.FtpConsts;
import org.code4everything.wetool.plugin.support.BaseViewController;
import org.code4everything.wetool.plugin.support.util.FxDialogs;
import org.code4everything.wetool.plugin.support.util.FxUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author pantao
 * @since 2019/8/25
 */
public abstract class AbstractDialogController implements BaseViewController {

    private final Map<String, List<String>> childrenMap = new HashMap<>(16);

    @FXML
    public ComboBox<String> ftpName;

    @FXML
    public ComboBox<String> remotePath;

    @FXML
    public TextField localPath;

    private boolean containsFile;

    void initialize(Set<String> ftpNames, String defaultFtpName, String defaultRemotePath, String defaultLocalPath,
                    boolean containsFile) {
        this.containsFile = containsFile;
        ftpName.getItems().addAll(ftpNames);
        ftpName.getSelectionModel().select(defaultFtpName);

        remotePath.setValue(defaultRemotePath);
        localPath.setText(defaultLocalPath);

        remotePath.getSelectionModel().selectedItemProperty().addListener((obs, old, nw) -> endCaretPosition());
    }

    /**
     * 选择本地文件
     */
    public abstract void choosePath();

    @Override
    public void dragFileOver(DragEvent event) {
        FxUtils.acceptCopyMode(event);
    }

    public void listChildren(KeyEvent keyEvent) {
        FxUtils.enterDo(keyEvent, () -> {
            if (FtpManager.isFtpNotSelected(ftpName)) {
                FxDialogs.showError(FtpConsts.SELECT_FTP);
                return;
            }
            endCaretPosition();
            String path = StrUtil.addSuffixIfNot(remotePath.getValue(), StringConsts.Sign.SLASH);
            List<String> children = childrenMap.get(path);
            if (CollUtil.isEmpty(children)) {
                // 从FTP服务器列出子目录
                children = FtpManager.listChildren(ftpName, path, containsFile);
                childrenMap.put(path, children);
            }
            remotePath.getItems().clear();
            remotePath.getItems().add(path);
            remotePath.getItems().addAll(children);
            remotePath.getSelectionModel().selectFirst();
            remotePath.show();
        });
    }

    /**
     * 将光标移到尾部
     */
    private void endCaretPosition() {
        // 通过失去焦点，获取焦点，片刻获取变化后的值
        ftpName.requestFocus();
        remotePath.requestFocus();
        remotePath.getEditor().positionCaret(Integer.MAX_VALUE);
    }
}
