package org.code4everything.wetool.plugin.devtool.ssh.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.swing.clipboard.ClipboardUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.ssh.Sftp;
import com.kodedu.terminalfx.TerminalBuilder;
import com.kodedu.terminalfx.TerminalTab;
import com.kodedu.terminalfx.config.TerminalConfig;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import org.code4everything.wetool.plugin.devtool.ssh.config.ServerConfiguration;
import org.code4everything.wetool.plugin.devtool.ssh.config.SftpFile;
import org.code4everything.wetool.plugin.devtool.ssh.config.SshConfiguration;
import org.code4everything.wetool.plugin.devtool.ssh.constant.CommonConsts;
import org.code4everything.wetool.plugin.devtool.ssh.ssh.SftpUtils;
import org.code4everything.wetool.plugin.support.BaseViewController;
import org.code4everything.wetool.plugin.support.factory.BeanFactory;
import org.code4everything.wetool.plugin.support.util.FxDialogs;
import org.code4everything.wetool.plugin.support.util.FxUtils;

import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author pantao
 * @since 2019/11/24
 */
public class MainController implements BaseViewController {

    private final TerminalConfig config = new TerminalConfig();

    private final Map<String, Integer> terminalCountMap = new HashMap<>();

    @FXML
    public ComboBox<String> serverCombo;

    @FXML
    public TextField currPathText;

    @FXML
    public TabPane terminalTabPane;

    @FXML
    public ListView<SftpFile> fileList;

    private String defaultConsolePath;

    @FXML
    private void initialize() {
        BeanFactory.registerView(CommonConsts.APP_ID, CommonConsts.APP_NAME, this);
        config.setCursorBlink(true);
        fileList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        reloadConfig();
    }

    public void openConfigFile() {
        String path = SshConfiguration.getPath();
        if (!FileUtil.exist(path)) {
            FileUtil.writeUtf8String("{\"defaultConsolePath\":null,\"servers\":[]}", path);
        }
        FxUtils.openFile(path);
    }

    public void reloadConfig() {
        // 清除数据
        terminalTabPane.getTabs().clear();
        fileList.getItems().clear();
        serverCombo.getItems().clear();
        currPathText.setText("");
        SftpUtils.clear();
        // 载入配置
        final SshConfiguration configuration = SshConfiguration.getConfiguration();
        defaultConsolePath = configuration.getDefaultConsolePath();
        if (CollUtil.isEmpty(configuration.getServers())) {
            return;
        }
        configuration.getServers().forEach(server -> {
            serverCombo.getItems().add(server.getAlias());
            SftpUtils.putConf(server);
        });
        serverCombo.getSelectionModel().select(0);
        openLocalTerminal();
    }

    public void openLocalTerminal() {
        openTerminal(null);
    }

    public void openRemoteTerminal() {
        openTerminal(SftpUtils.getConf(serverCombo.getValue()));
    }

    public void uploadOnBtn() {
        String dir = StrUtil.emptyToDefault(currPathText.getText(), "/");
        FxUtils.chooseFile(file -> upload(file.getAbsolutePath(), dir));
    }

    public void downloadAndOpen() {
        download(true);
    }

    public void uploadOnMenu() {
        final ObservableList<SftpFile> list = fileList.getSelectionModel().getSelectedItems();
        for (SftpFile file : list) {
            if (file.getIsDir()) {
                FxUtils.chooseFile(f -> upload(f.getAbsolutePath(), file.getPath()));
                break;
            }
        }
    }

    public void download() {
        download(false);
    }

    public void delete() {
        final ObservableList<SftpFile> list = fileList.getSelectionModel().getSelectedItems();
        final Sftp sftp = SftpUtils.getSftp(serverCombo.getValue());
        List<SftpFile> deleted = new ArrayList<>(list.size());
        list.forEach(file -> {
            boolean res;
            if (file.getIsDir()) {
                res = sftp.delDir(file.getPath());
            } else {
                res = sftp.delFile(file.getPath());
            }
            if (res) {
                deleted.add(file);
            }
        });
        fileList.getItems().removeAll(deleted);
    }

    public void copyPath() {
        ClipboardUtil.setStr(fileList.getSelectionModel().getSelectedItem().getPath());
    }

    public void listFiles(KeyEvent keyEvent) {
        FxUtils.enterDo(keyEvent, () -> listFiles(currPathText.getText()));
    }

    public void listIfDir(MouseEvent mouseEvent) {
        FxUtils.doubleClicked(mouseEvent, () -> {
            final SftpFile sftpFile = fileList.getSelectionModel().getSelectedItem();
            if (sftpFile.getIsDir()) {
                currPathText.setText(currPathText + "/" + sftpFile.getPath());
                listFiles(sftpFile.getPath());
            }
        });
    }

    private void download(boolean open) {
        final ObservableList<SftpFile> list = fileList.getSelectionModel().getSelectedItems();
        final Sftp sftp = SftpUtils.getSftp(serverCombo.getValue());
        List<String> downloaded = new ArrayList<>(list.size());
        FxUtils.chooseFolder(folder -> list.forEach(f -> {
            sftp.download(f.getPath(), folder);
        }));
        FxDialogs.showInformation("下载成功！", null);
    }

    private void listFiles(String dir) {
        dir = StrUtil.emptyToDefault(dir, "/");
        final Sftp sftp = SftpUtils.getSftp(serverCombo.getValue());
        fileList.getItems().clear();
        // 文件夹
        final List<String> dirs = ObjectUtil.defaultIfNull(sftp.lsDirs(dir), Collections.emptyList());
        fileList.getItems().addAll(dirs.stream().map(d -> new SftpFile(d, true)).collect(Collectors.toList()));
        // 文件
        final List<String> files = ObjectUtil.defaultIfNull(sftp.lsFiles(dir), Collections.emptyList());
        fileList.getItems().addAll(files.stream().map(f -> new SftpFile(f, false)).collect(Collectors.toList()));
    }

    private void openTerminal(ServerConfiguration server) {
        TerminalBuilder terminalBuilder = new TerminalBuilder(config);
        if (Objects.isNull(server)) {
            terminalBuilder.setTerminalPath(Paths.get(defaultConsolePath));
        }
        TerminalTab terminal = terminalBuilder.newTerminal();
        if (Objects.isNull(server)) {
            terminal.setText(fmtTerminalText("Local"));
        } else {
            terminal.setText(fmtTerminalText(server.getAlias()));
            terminal.onTerminalFxReady(() -> {
                String cmd = StrUtil.format("ssh {}@{}\r", server.getUsername(), server.getHost());
                terminal.getTerminal().command(cmd);
                ClipboardUtil.setStr(server.getPassword());
            });
        }
        terminalTabPane.getTabs().add(terminal);
        terminalTabPane.getSelectionModel().select(terminal);
    }

    private String fmtTerminalText(String server) {
        final Integer integer = terminalCountMap.getOrDefault(server, 0);
        terminalCountMap.put(server, integer + 1);
        return StrUtil.format("{} #{}", server, integer);
    }

    private void upload(String localPath, String destPath) {
        final Sftp sftp = SftpUtils.getSftp(serverCombo.getValue());
        sftp.put(localPath, destPath);
        FxDialogs.showInformation("上传成功！", null);
    }
}
