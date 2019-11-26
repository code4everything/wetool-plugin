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
import javafx.event.Event;
import javafx.event.EventHandler;
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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author pantao
 * @since 2019/11/24
 */
public class MainController implements BaseViewController {

    private final TerminalConfig config = new TerminalConfig();

    private final Map<String, Integer> terminalCountMap = new HashMap<>();

    private final SftpFile textDir = new SftpFile(null, true);

    private final EventHandler<Event> noAction = e -> {};

    @FXML
    public ComboBox<String> serverCombo;

    @FXML
    public TextField currPathText;

    @FXML
    public TabPane terminalTabPane;

    @FXML
    public ListView<SftpFile> fileList;

    private Pattern pattern = null;

    private String defaultConsolePath;

    private String localCharset;

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
        localCharset = configuration.getLocalCharset().toString();
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
        openTerminal(null, localCharset);
    }

    public void openRemoteTerminal() {
        final ServerConfiguration conf = SftpUtils.getConf(serverCombo.getValue());
        openTerminal(conf, conf.getCharset().toString());
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
        if (CollUtil.isEmpty(list)) {
            return;
        }
        for (SftpFile file : list) {
            if (file.isDir()) {
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
        if (CollUtil.isEmpty(list)) {
            return;
        }
        final Sftp sftp = SftpUtils.getSftp(serverCombo.getValue());
        List<SftpFile> deleted = new ArrayList<>(list.size());
        list.forEach(file -> {
            boolean res;
            if (file.isDir()) {
                res = sftp.delDir(file.getPath());
            } else {
                res = sftp.delFile(file.getPath());
            }
            if (res) {
                deleted.add(file);
            }
        });
        fileList.getItems().removeAll(deleted);
        FxDialogs.showInformation("删除成功！", null);
    }

    public void copyPath() {
        SftpFile sftpFile = fileList.getSelectionModel().getSelectedItem();
        if (Objects.isNull(sftpFile)) {
            return;
        }
        ClipboardUtil.setStr(sftpFile.getPath());
    }

    public void listFiles(KeyEvent keyEvent) {
        FxUtils.enterDo(keyEvent, () -> {
            List<String> list = StrUtil.splitTrim(StrUtil.emptyToDefault(currPathText.getText(), "/"), ':');
            pattern = null;
            if (list.size() > 1) {
                try {
                    pattern = Pattern.compile(list.get(1));
                } catch (Exception e) {
                    // ignore
                }
            }
            textDir.setPath(list.get(0));
            listFiles(textDir);
        });
    }

    public void listIfDir(MouseEvent mouseEvent) {
        FxUtils.doubleClicked(mouseEvent, () -> {
            final SftpFile sftpFile = fileList.getSelectionModel().getSelectedItem();
            if (ObjectUtil.isNotNull(sftpFile) && sftpFile.isDir()) {
                currPathText.setText(sftpFile.getPath());
                pattern = null;
                listFiles(sftpFile);
            }
        });
    }

    private void download(boolean open) {
        final ObservableList<SftpFile> list = fileList.getSelectionModel().getSelectedItems();
        if (CollUtil.isEmpty(list)) {
            return;
        }
        final Sftp sftp = SftpUtils.getSftp(serverCombo.getValue());
        List<String> downloaded = new ArrayList<>(list.size());
        FxUtils.chooseFolder(folder -> list.forEach(f -> {
            if (!f.isDir()) {
                String path = StrUtil.addPrefixIfNot(f.getPath(), "/");
                sftp.download(f.getPath(), folder);
                downloaded.add(folder.getAbsolutePath() + path.substring(path.lastIndexOf("/")));
            }
        }));
        if (open) {
            downloaded.forEach(FxUtils::openFile);
        } else {
            FxDialogs.showInformation("下载成功！", null);
        }
    }

    private void listFiles(SftpFile dir) {
        final Sftp sftp = SftpUtils.getSftp(serverCombo.getValue());
        fileList.getItems().clear();
        // 文件夹
        List<String> dirs = ObjectUtil.defaultIfNull(sftp.lsDirs(dir.getPath()), Collections.emptyList());
        dirs = filterFile(dirs);
        fileList.getItems().addAll(dirs.stream().map(d -> SftpFile.of(dir, d, true)).collect(Collectors.toList()));
        // 文件
        List<String> files = ObjectUtil.defaultIfNull(sftp.lsFiles(dir.getPath()), Collections.emptyList());
        files = filterFile(files);
        fileList.getItems().addAll(files.stream().map(f -> SftpFile.of(dir, f, false)).collect(Collectors.toList()));
    }

    private List<String> filterFile(List<String> files) {
        if (CollUtil.isEmpty(files)) {
            return Collections.emptyList();
        }
        return files.stream().filter(s -> Objects.isNull(pattern) || pattern.matcher(s).find()).collect(Collectors.toList());
    }

    private void openTerminal(ServerConfiguration server, String charset) {
        config.setReceiveEncoding(charset);
        config.setSendEncoding(charset);
        TerminalBuilder terminalBuilder = new TerminalBuilder(config);
        terminalBuilder.setTerminalPath(Paths.get(defaultConsolePath));
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
        terminal.setOnCloseRequest(noAction);
        terminalTabPane.getTabs().add(terminal);
        terminalTabPane.getSelectionModel().select(terminal);
    }

    private String fmtTerminalText(String server) {
        final Integer integer = terminalCountMap.getOrDefault(server, 1);
        terminalCountMap.put(server, integer + 1);
        return StrUtil.format("{} #{}", server, integer);
    }

    private void upload(String localPath, String destPath) {
        final Sftp sftp = SftpUtils.getSftp(serverCombo.getValue());
        sftp.put(localPath, destPath);
        FxDialogs.showInformation("上传成功！", null);
    }
}
