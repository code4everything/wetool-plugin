package org.code4everything.wetool.plugin.qiniu.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.swing.clipboard.ClipboardUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.qiniu.common.QiniuException;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.AreaChart;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.DragEvent;
import javafx.util.Pair;
import org.code4everything.boot.base.DateUtils;
import org.code4everything.wetool.plugin.qiniu.api.SdkConfigurer;
import org.code4everything.wetool.plugin.qiniu.api.SdkManager;
import org.code4everything.wetool.plugin.qiniu.constant.QiniuConsts;
import org.code4everything.wetool.plugin.qiniu.model.ConfigBean;
import org.code4everything.wetool.plugin.qiniu.model.FileBean;
import org.code4everything.wetool.plugin.qiniu.service.QiniuService;
import org.code4everything.wetool.plugin.qiniu.util.ConfigUtils;
import org.code4everything.wetool.plugin.qiniu.util.DialogUtils;
import org.code4everything.wetool.plugin.qiniu.util.QiniuDialog;
import org.code4everything.wetool.plugin.qiniu.util.QiniuUtils;
import org.code4everything.wetool.plugin.support.factory.BeanFactory;
import org.code4everything.wetool.plugin.support.util.FxUtils;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * 界面控制类
 *
 * @author pantao
 */
public class MainController extends BaseQiniuController {

    private static final String UPLOAD_STATUS_TEMPLATE = "{}\tsuccess\t{}{}\t{}";

    private final QiniuService service = new QiniuService();

    private final QiniuDialog dialog = new QiniuDialog();

    @FXML
    public ComboBox<String> bucketCB;

    @FXML
    public TextField zoneTF;

    @FXML
    public TextArea uploadStatusTA;

    @FXML
    public ComboBox<String> prefixCB;

    @FXML
    public TableView<FileBean> resTV;

    @FXML
    public TextField searchTF;

    @FXML
    public CheckBox recursiveCB;

    @FXML
    public CheckBox keepPathCB;

    private ObservableList<FileBean> resData = null;

    /**
     * 空间总文件数
     */
    private int dataLength = 0;

    /**
     * 空间使用总大小
     */
    private long dataSize = 0;

    @FXML
    private TextArea selectedFileTA;

    @FXML
    private TextField domainTF;

    @FXML
    private TableColumn<FileBean, String> nameTC;

    @FXML
    private TableColumn<FileBean, String> typeTC;

    @FXML
    private TableColumn<FileBean, String> sizeTC;

    @FXML
    private TableColumn<FileBean, String> timeTC;

    @FXML
    private Label sizeLabel;

    @FXML
    private Label lengthLabel;

    @FXML
    private AreaChart<String, Long> fluxAC;

    @FXML
    private AreaChart<String, Long> bandwidthAC;

    @FXML
    private DatePicker startDP;

    @FXML
    private DatePicker endDP;

    @FXML
    private ComboBox<String> fluxUnitCB;

    @FXML
    private ComboBox<String> bandwidthUnitCB;

    private String status = "";

    /**
     * 父文件夹路径
     */
    private List<String> rootPath = new ArrayList<>();

    public static MainController getInstance() {
        return BeanFactory.get(MainController.class);
    }

    public ObservableList<FileBean> getResData() {
        return resData;
    }

    public void setResData(ObservableList<FileBean> resData) {
        this.resData = resData;
    }

    public int getDataLength() {
        return dataLength;
    }

    public void setDataLength(int dataLength) {
        this.dataLength = dataLength;
    }

    public long getDataSize() {
        return dataSize;
    }

    public void setDataSize(long dataSize) {
        this.dataSize = dataSize;
    }

    /**
     * 初始化
     */
    @FXML
    private void initialize() {
        BeanFactory.registerView(QiniuConsts.TAB_ID, QiniuConsts.TAB_NAME, this);
        nameTC.setCellValueFactory(new PropertyValueFactory<>("name"));
        // 设置文件名可编辑
        nameTC.setCellFactory(TextFieldTableCell.forTableColumn());
        nameTC.setOnEditCommit(value -> {
            String name;
            FileBean fileBean = value.getTableView().getItems().get(value.getTablePosition().getRow());
            // 编辑后重命名文件
            if (service.renameFile(bucketCB.getValue(), value.getOldValue(), value.getNewValue())) {
                name = value.getNewValue();
            } else {
                name = value.getOldValue();
            }
            if (StrUtil.isNotEmpty(searchTF.getText())) {
                resData.get(resData.indexOf(fileBean)).setName(name);
            }
            fileBean.setName(name);
        });
        typeTC.setCellValueFactory(new PropertyValueFactory<>("type"));
        // 设置文件类型可编辑
        typeTC.setCellFactory(TextFieldTableCell.forTableColumn());
        typeTC.setOnEditCommit(value -> {
            String type;
            FileBean fileBean = value.getTableView().getItems().get(value.getTablePosition().getRow());
            // 编辑后更新文件类型
            if (service.changeType(fileBean.getName(), value.getNewValue(), bucketCB.getValue())) {
                type = value.getNewValue();
            } else {
                type = value.getOldValue();
            }
            if (StrUtil.isNotEmpty(searchTF.getText())) {
                resData.get(resData.indexOf(fileBean)).setType(type);
            }
            fileBean.setType(type);
        });
        sizeTC.setCellValueFactory(new PropertyValueFactory<>("size"));
        timeTC.setCellValueFactory(new PropertyValueFactory<>("time"));
        // 设置表格允许多选
        resTV.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        // 设置默认的开始和结束日期，并事件刷新数据
        endDP.setValue(LocalDate.now());
        long startTime = System.currentTimeMillis() - QiniuConsts.DATE_SPAN_OF_THIRTY_ONE;
        LocalDate localEndDate = DateUtils.toLocalDate(new Date(startTime));
        startDP.setValue(localEndDate);
        // 设置桶下拉框改变事件，改变后配置新的上传环境
        bucketCB.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            zoneTF.setText(ConfigBean.getConfig().getZone(newValue));
            searchTF.clear();
            String url = ConfigBean.getConfig().getUrl(newValue);
            if (Validator.URL_HTTP.matcher(url).matches()) {
                domainTF.setText(url);
            } else {
                domainTF.setText(QiniuConsts.DOMAIN_CONFIG_ERROR);
            }
            ThreadUtil.execute(() -> {
                if (SdkConfigurer.configUploadEnv(ConfigBean.getConfig().getZone(newValue), newValue)) {
                    // 加载文件列表
                    mapResourceData();
                    // 刷新流量带宽统计
                    dateChange();
                }
            });
        });
        // 初始化统计单位选择框
        fluxUnitCB.getItems().addAll("KB", "MB", "GB", "TB");
        fluxUnitCB.setValue("KB");
        fluxUnitCB.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> drawChart(true, false));
        bandwidthUnitCB.getItems().addAll(fluxUnitCB.getItems());
        bandwidthUnitCB.setValue("KB");
        bandwidthUnitCB.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> drawChart(false, true));
    }

    /**
     * 开始拖曳文件
     */
    @Override
    public void dragFileOver(DragEvent event) {
        FxUtils.acceptCopyMode(event);
    }

    /**
     * 拖曳文件松开鼠标
     */
    @Override
    public void dragFileDropped(DragEvent event) {
        FxUtils.dropFiles(event, this::appendFile);
    }

    /**
     * 开始日期或结束日期改变，刷新流量、带宽统计
     */
    public void dateChange() {
        drawChart(true, true);
    }

    /**
     * 绘制数据统计图表
     */
    private void drawChart(boolean isFluxUnitChange, boolean isBandwidthUnitChange) {
        Date localStartDate = DateUtils.toDate(startDP.getValue());
        Date localEndDate = DateUtils.toDate(endDP.getValue());
        // 将本地日期装换成字符串
        String fromDate = DateUtil.formatDate(localStartDate);
        String toDate = DateUtil.formatDate(localEndDate);
        // 获取开始日期和结束日期的时间差
        long timeSpan = localEndDate.getTime() - localStartDate.getTime();
        if (StrUtil.isNotEmpty(domainTF.getText()) && timeSpan >= 0 && timeSpan <= QiniuConsts.DATE_SPAN_OF_THIRTY_ONE) {
            Platform.runLater(() -> {
                String[] domains = {domainTF.getText()};
                if (isFluxUnitChange) {
                    // 获取流量数据
                    String fluxUnit = fluxUnitCB.getValue();
                    fluxAC.getData().clear();
                    fluxAC.getData().add(service.getBucketFlux(domains, fromDate, toDate, fluxUnit));
                }
                if (isBandwidthUnitChange) {
                    // 获取带宽数据
                    String bandUnit = bandwidthUnitCB.getValue();
                    bandwidthAC.getData().clear();
                    bandwidthAC.getData().add(service.getBucketBandwidth(domains, fromDate, toDate, bandUnit));
                }
            });
        }
    }

    /**
     * 下载日志
     */
    public void downloadCdnLog() {
        String date = DialogUtils.showInputDialog(null, QiniuConsts.INPUT_LOG_DATE, DateUtil.formatDate(new Date()));
        service.downloadCdnLog(date);
    }

    /**
     * 刷新文件
     */
    public void refreshFile() {
        service.refreshFile(resTV.getSelectionModel().getSelectedItems(), domainTF.getText());
    }

    /**
     * 用浏览器打开文件
     */
    public void openFile() {
        ObservableList<FileBean> selectedItems = resTV.getSelectionModel().getSelectedItems();
        if (CollUtil.isNotEmpty(selectedItems)) {
            String filename = selectedItems.get(0).getName();
            FxUtils.openLink(QiniuUtils.buildUrl(filename, domainTF.getText()));
        }
    }

    /**
     * 私有下载
     */
    public void privateDownload() {
        download(DownloadWay.PRIVATE);
    }

    /**
     * 下载文件
     */
    private void download(DownloadWay way) {
        ObservableList<FileBean> selectedItems = resTV.getSelectionModel().getSelectedItems();
        if (CollUtil.isNotEmpty(selectedItems)) {
            if (way == DownloadWay.PUBLIC) {
                selectedItems.forEach(bean -> service.publicDownload(bean.getName(), domainTF.getText()));
            } else {
                selectedItems.forEach(bean -> service.privateDownload(bean.getName(), domainTF.getText()));
            }
        }
    }

    /**
     * 公有下载
     */
    public void publicDownload() {
        download(DownloadWay.PUBLIC);
    }

    /**
     * 更新镜像源
     */
    public void updateFile() {
        ObservableList<FileBean> selectedItems = resTV.getSelectionModel().getSelectedItems();
        if (CollUtil.isNotEmpty(selectedItems)) {
            selectedItems.forEach(bean -> service.updateFile(bucketCB.getValue(), bean.getName()));
        }
    }

    /**
     * 设置文件生存时间
     */
    public void setLife() {
        ObservableList<FileBean> selectedItems = resTV.getSelectionModel().getSelectedItems();
        if (CollUtil.isNotEmpty(selectedItems)) {
            // 弹出输入框
            String fileLife = DialogUtils.showInputDialog(null, QiniuConsts.FILE_LIFE, QiniuConsts.DEFAULT_FILE_LIFE);
            if (NumberUtil.isInteger(fileLife)) {
                int life = Integer.parseInt(fileLife);
                selectedItems.forEach(bean -> service.setFileLife(bucketCB.getValue(), bean.getName(), life));
            }
        }
    }

    /**
     * 显示移动或复制文件的弹窗
     */
    public void showFileMovableDialog() {
        ObservableList<FileBean> selectedItems = resTV.getSelectionModel().getSelectedItems();
        if (CollUtil.isEmpty(selectedItems)) {
            // 没有选择文件，结束方法
            return;
        }
        Pair<SdkManager.FileAction, String[]> resultPair;
        String bucket = bucketCB.getValue();
        if (selectedItems.size() > 1) {
            resultPair = dialog.showFileDialog(bucket, "", false);
        } else {
            resultPair = dialog.showFileDialog(bucket, selectedItems.get(0).getName(), true);
        }
        if (ObjectUtil.isNotNull(resultPair)) {
            boolean useNewKey = ArrayUtil.isNotEmpty(resultPair.getValue()[1]);
            ObservableList<FileBean> fileBeans = resTV.getItems();
            for (FileBean fileBean : selectedItems) {
                String fromBucket = bucketCB.getValue();
                String toBucket = resultPair.getValue()[0];
                String name = useNewKey ? resultPair.getValue()[1] : fileBean.getName();
                boolean isSuccess = service.moveOrCopyFile(fromBucket, fileBean.getName(), toBucket, name,
                        resultPair.getKey());
                if (resultPair.getKey() == SdkManager.FileAction.MOVE && isSuccess) {
                    boolean isInSearch = StrUtil.isNotEmpty(searchTF.getText());
                    if (fromBucket.equals(toBucket)) {
                        // 更新文件名
                        fileBean.setName(name);
                        if (isInSearch) {
                            fileBeans.get(fileBeans.indexOf(fileBean)).setName(name);
                        }
                    } else {
                        // 删除数据源
                        fileBeans.remove(fileBean);
                        dataLength--;
                        dataSize -= QiniuUtils.sizeToLong(fileBean.getSize());
                        if (isInSearch) {
                            fileBeans.remove(fileBean);
                        }
                    }
                }
            }
            countBucket();
        }
    }

    /**
     * 删除文件
     */
    public void deleteFile() {
        service.deleteFile(resTV.getSelectionModel().getSelectedItems(), bucketCB.getValue());
    }

    /**
     * 复制链接
     */
    public void copyLink() {
        ObservableList<FileBean> fileBeans = resTV.getSelectionModel().getSelectedItems();
        if (CollUtil.isNotEmpty(fileBeans)) {
            // 只复制选中的第一个文件的链接
            ClipboardUtil.setStr(QiniuUtils.buildUrl(fileBeans.get(0).getName(), domainTF.getText()));
        }
    }

    /**
     * 搜索资源文件，忽略大小写
     */
    public void searchFile() {
        ArrayList<FileBean> files = new ArrayList<>();
        String search = StrUtil.nullToEmpty(searchTF.getText());
        dataLength = 0;
        dataSize = 0;
        // 正则匹配查询
        Pattern pattern;
        try {
            pattern = Pattern.compile(search, Pattern.CASE_INSENSITIVE);
        } catch (Exception e) {
            // 忽略不正确的正则表达式
            return;
        }
        for (FileBean file : resData) {
            if (pattern.matcher(file.getName()).find()) {
                files.add(file);
                dataLength++;
                dataSize += QiniuUtils.sizeToLong(file.getSize());
            }
        }
        countBucket();
        resTV.setItems(FXCollections.observableArrayList(files));
    }

    /**
     * 统计空间文件的数量以及大小
     */
    public void countBucket() {
        lengthLabel.setText(NumberUtil.decimalFormat(",###", dataLength) + " 个文件");
        sizeLabel.setText(FileUtil.readableFileSize(dataSize));
    }

    /**
     * 刷新资源列表
     */
    public void refreshResourceData() {
        mapResourceData();
        DialogUtils.showInformation(QiniuConsts.REFRESH_SUCCESS);
    }

    /**
     * 将从存储空间获取的文件列表映射到表中
     */
    private void mapResourceData() {
        ThreadUtil.execute(() -> {
            // 列出资源文件
            service.listFile();
            Platform.runLater(() -> {
                resTV.setItems(resData);
                countBucket();
            });
        });

    }

    /**
     * 添加桶至下拉框
     */
    public void appendBucket(String bucket) {
        if (!bucketCB.getItems().contains(bucket)) {
            bucketCB.getItems().add(bucket);
        }
    }

    /**
     * 保存文件的上传状态
     */
    public void saveUploadStatus() {
        FxUtils.saveFile(file -> QiniuUtils.saveFile(file, uploadStatusTA.getText()));
    }

    /**
     * 复制文件上传状态至剪贴板
     */
    public void copyUploadStatus() {
        ClipboardUtil.setStr(uploadStatusTA.getText());
    }

    /**
     * 清空文件的上传状态
     */
    public void clearUploadStatus() {
        uploadStatusTA.clear();
    }

    /**
     * 显示选择文件的弹窗
     */
    public void showOpenFileDialog() {
        FxUtils.chooseFiles(this::appendFile);
    }

    @Override
    public void openFolder(File folder) {
        appendFile(folder.listFiles(), false);
    }

    @Override
    public void openMultiFiles(List<File> files) {
        appendFile(files);
    }

    @Override
    public void openFile(File file) {
        openMultiFiles(Lists.newArrayList(file));
    }

    /**
     * 添加上传的文件，支持拖曳文件夹
     */
    private void appendFile(List<File> files) {
        File[] fileArray = new File[files.size()];
        appendFile(files.toArray(fileArray), false);
    }

    /**
     * 添加上传的文件，支持拖曳文件夹
     */
    private void appendFile(@Nullable File[] files, boolean isRecursive) {
        if (ArrayUtil.isNotEmpty(files)) {
            for (File file : files) {
                if (file.isDirectory()) {
                    if (isRecursive) {
                        // 递归添加文件
                        if (recursiveCB.isSelected()) {
                            appendFile(file.listFiles(), true);
                        }
                    } else {
                        rootPath.add(file.getAbsolutePath());
                        appendFile(file.listFiles(), true);
                    }
                } else if (!selectedFileTA.getText().contains(file.getAbsolutePath())) {
                    selectedFileTA.insertText(0, file.getAbsolutePath() + "\r\n");
                }
            }
        }
    }

    /**
     * 上传选择的文件
     */
    public void uploadFile() {
        if (StrUtil.isEmpty(zoneTF.getText()) || StrUtil.isEmpty(selectedFileTA.getText())) {
            // 没有选择存储空间或文件，不能上传文件
            DialogUtils.showWarning(QiniuConsts.NEED_CHOOSE_BUCKET_OR_FILE);
            return;
        }
        // 新建一个线程上传文件的线程
        ThreadUtil.execute(() -> {
            Platform.runLater(() -> uploadStatusTA.insertText(0, QiniuConsts.CONFIG_UPLOAD_ENVIRONMENT));
            String bucket = bucketCB.getValue();
            // 默认不指定KEY的情况下，以文件内容的哈希值作为文件名
            String key = StrUtil.nullToEmpty(prefixCB.getValue());
            String[] paths = selectedFileTA.getText().split("\n");
            // 去掉\r\n的长度
            int endIndex = QiniuConsts.UPLOADING.length() - 2;
            Platform.runLater(() -> uploadStatusTA.deleteText(0, QiniuConsts.CONFIG_UPLOAD_ENVIRONMENT.length() - 1));
            // 总文件数
            for (String path : paths) {
                if (StrUtil.isNotEmpty(path)) {
                    Platform.runLater(() -> uploadStatusTA.insertText(0, QiniuConsts.UPLOADING));
                    String filename = "";
                    String url = "http://" + ConfigBean.getConfig().getUrl(bucket) + "/";
                    File file = new File(path);
                    try {
                        // 判断文件是否存在
                        if (file.exists()) {
                            // 保持文件相对父文件夹的路径
                            if (keepPathCB.isSelected() && CollUtil.isNotEmpty(rootPath)) {
                                for (String root : rootPath) {
                                    if (file.getAbsolutePath().startsWith(root)) {
                                        String postKey = root.substring(root.lastIndexOf(File.separator) + 1);
                                        filename = key + postKey + file.getAbsolutePath().substring(root.length());
                                        break;
                                    }
                                }
                            }
                            if (StrUtil.isEmpty(filename)) {
                                filename = key + file.getName();
                            }
                            service.uploadFile(bucket, path, filename);
                            String now = DateUtil.formatDate(new Date());
                            status = StrUtil.format(UPLOAD_STATUS_TEMPLATE, now, url, filename, path);
                        } else if (Validator.URL_HTTP.matcher(path).matches()) {
                            // 抓取网络文件到空间中
                            filename = key + FileUtil.getName(path);
                            SdkConfigurer.getBucketManager().fetch(path, bucket, filename);
                            String now = DateUtil.formatDate(new Date());
                            status = StrUtil.format(UPLOAD_STATUS_TEMPLATE, now, url, filename, path);
                        } else {
                            // 文件不存在
                            status = DateUtil.formatDate(new Date()) + "\tfailed\t" + path;
                        }
                    } catch (QiniuException e) {
                        status = DateUtil.formatDate(new Date()) + "\terror\t" + path;
                        Platform.runLater(() -> DialogUtils.showException(QiniuConsts.UPLOAD_ERROR, e));
                    }
                    Platform.runLater(() -> {
                        uploadStatusTA.deleteText(0, endIndex);
                        uploadStatusTA.insertText(0, status);
                    });
                }
                Platform.runLater(() -> selectedFileTA.deleteText(0, path.length() + (paths.length > 1 ? 1 : 0)));
            }
            rootPath.clear();
            Platform.runLater(() -> {
                // 将光标移到最前面
                uploadStatusTA.positionCaret(0);
                // 清空待上传的文件列表
                selectedFileTA.clear();
            });
            mapResourceData();
            // 添加文件前缀到配置文件
            savePrefix(key);
        });
    }

    /**
     * 保存前缀
     */
    private void savePrefix(String key) {
        if (StrUtil.isNotEmpty(key) && !ConfigBean.getConfig().getPrefixes().contains(key)) {
            Platform.runLater(() -> prefixCB.getItems().add(key));
            ConfigBean.getConfig().getPrefixes().add(key);
            ConfigUtils.writeConfig();
        }
    }

    /**
     * 打开配置文件
     */
    public void openConfigFile() {
        try {
            Desktop.getDesktop().open(new File(QiniuConsts.CONFIG_PATH));
            // 用户触发是否重载配置文件
            Optional<ButtonType> result = DialogUtils.showConfirmation(QiniuConsts.RELOAD_CONFIG);
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // 重新载入配置文件
                bucketCB.getItems().clear();
                prefixCB.getItems().clear();
                ConfigUtils.loadConfig();
            }
        } catch (Exception e) {
            DialogUtils.showException(QiniuConsts.OPEN_FILE_ERROR, e);
        }
    }

    /**
     * 显示重置密钥的弹窗
     */
    public void showKeyDialog() {
        boolean ok = dialog.showKeyDialog();
        if (ok && StrUtil.isNotEmpty(zoneTF.getText())) {
            // 配置新的环境
            SdkConfigurer.configUploadEnv(zoneTF.getText(), bucketCB.getValue());
        }
    }

    /**
     * 显示添加存储空间的弹窗
     */
    public void showBucketDialog() {
        dialog.showBucketDialog();
    }

    public enum DownloadWay {
        // 下载的方式，包括私有和公有
        PRIVATE, PUBLIC
    }
}
