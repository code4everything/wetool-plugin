package org.code4everything.wetool.plugin.qiniu.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.qiniu.cdn.CdnResult;
import com.qiniu.common.QiniuException;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.model.BatchStatus;
import com.qiniu.storage.model.FileInfo;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import lombok.extern.slf4j.Slf4j;
import org.code4everything.wetool.plugin.qiniu.api.SdkConfigurer;
import org.code4everything.wetool.plugin.qiniu.api.SdkManager;
import org.code4everything.wetool.plugin.qiniu.constant.QiniuConsts;
import org.code4everything.wetool.plugin.qiniu.controller.MainController;
import org.code4everything.wetool.plugin.qiniu.model.ConfigBean;
import org.code4everything.wetool.plugin.qiniu.model.FileBean;
import org.code4everything.wetool.plugin.qiniu.util.DialogUtils;
import org.code4everything.wetool.plugin.qiniu.util.QiniuUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

/**
 * 七牛服务类
 *
 * @author pantao
 * @since 2018/11/13
 */
@Slf4j
public class QiniuService {

    private final SdkManager sdkManager = new SdkManager();

    /**
     * 上传文件
     */
    public void uploadFile(String bucket, String key, String filename) throws QiniuException {
        String upToken = SdkConfigurer.getAuth().uploadToken(bucket, filename);
        SdkConfigurer.getUploadManager().put(key, filename, upToken);
    }

    /**
     * 获取空间文件列表
     */
    public void listFile() {
        MainController main = MainController.getInstance();
        // 列举空间文件列表
        BucketManager.FileListIterator iterator = sdkManager.getFileListIterator(main.bucketCB.getValue());
        ArrayList<FileBean> files = new ArrayList<>();
        main.setDataLength(0);
        main.setDataSize(0);
        // 处理结果
        while (iterator.hasNext()) {
            FileInfo[] items = iterator.next();
            for (FileInfo item : items) {
                main.setDataLength(main.getDataLength() + 1);
                main.setDataSize(main.getDataSize() + item.fsize);
                // 将七牛的时间单位（100纳秒）转换成毫秒，然后转换成时间
                String time = DateUtil.formatDateTime(new Date(item.putTime / 10000));
                String size = FileUtil.readableFileSize(item.fsize);
                FileBean file = new FileBean(item.key, item.mimeType, size, time);
                files.add(file);
            }
        }
        main.setResData(FXCollections.observableArrayList(files));
    }

    /**
     * 批量删除文件，单次批量请求的文件数量不得超过1000
     */
    public void deleteFile(ObservableList<FileBean> fileBeans, String bucket) {
        if (CollUtil.isNotEmpty(fileBeans) && QiniuUtils.checkNet()) {
            // 生成待删除的文件列表
            String[] files = new String[fileBeans.size()];
            ArrayList<FileBean> selectedFiles = new ArrayList<>();
            int i = 0;
            for (FileBean fileBean : fileBeans) {
                files[i++] = fileBean.getName();
                selectedFiles.add(fileBean);
            }
            try {
                BatchStatus[] batchStatusList = sdkManager.batchDelete(bucket, files);
                MainController main = MainController.getInstance();
                // 文件列表是否为搜索后结果
                boolean isInSearch = StrUtil.isNotEmpty(main.searchTF.getText());
                ObservableList<FileBean> currentRes = main.resTV.getItems();
                // 更新界面数据
                for (i = 0; i < files.length; i++) {
                    BatchStatus status = batchStatusList[i];
                    String file = files[i];
                    if (status.code == 200) {
                        main.getResData().remove(selectedFiles.get(i));
                        main.setDataLength(main.getDataLength() - 1);
                        main.setDataSize(main.getDataSize() - QiniuUtils.sizeToLong(selectedFiles.get(i).getSize()));
                        if (isInSearch) {
                            currentRes.remove(selectedFiles.get(i));
                        }
                    } else {
                        log.error("delete " + file + " failed, message -> " + status.data.error);
                        DialogUtils.showError("删除文件：" + file + " 失败");
                    }
                }
            } catch (QiniuException e) {
                DialogUtils.showException(QiniuConsts.DELETE_ERROR, e);
            }
            MainController.getInstance().countBucket();
        }
    }

    /**
     * 修改文件类型
     */
    public boolean changeType(String fileName, String newType, String bucket) {
        boolean result = true;
        try {
            sdkManager.changeMime(bucket, fileName, newType);
        } catch (QiniuException e) {
            DialogUtils.showException(QiniuConsts.CHANGE_FILE_TYPE_ERROR, e);
            result = false;
        }
        return result;
    }

    /**
     * 重命名文件
     */
    public boolean renameFile(String bucket, String oldName, String newName) {
        return moveFile(bucket, oldName, bucket, newName);
    }

    /**
     * 移动文件
     */
    private boolean moveFile(String srcBucket, String fromKey, String destBucket, String toKey) {
        return moveOrCopyFile(srcBucket, fromKey, destBucket, toKey, SdkManager.FileAction.MOVE);
    }

    /**
     * 移动或复制文件
     */
    public boolean moveOrCopyFile(String srcBucket, String srcKey, String destBucket, String destKey,
                                  SdkManager.FileAction fileAction) {
        boolean result = true;
        try {
            sdkManager.moveOrCopyFile(srcBucket, srcKey, destBucket, destKey, fileAction);
        } catch (QiniuException e) {
            log.error("move file failed, message -> " + e.getMessage());
            DialogUtils.showException(QiniuConsts.MOVE_OR_RENAME_ERROR, e);
            result = false;
        }
        return result;
    }

    /**
     * 设置文件生存时间
     */
    public void setFileLife(String bucket, String key, int days) {
        try {
            sdkManager.deleteAfterDays(bucket, key, days);
            log.info("set file life success");
        } catch (QiniuException e) {
            log.error("set file life error, message -> " + e.getMessage());
            DialogUtils.showException(QiniuConsts.MOVE_OR_RENAME_ERROR, e);
        }
    }

    /**
     * 更新镜像源
     */
    public void updateFile(String bucket, String key) {
        try {
            sdkManager.prefetch(bucket, key);
            log.info("prefetch files success");
        } catch (QiniuException e) {
            log.error("prefetch files error, message -> " + e.getMessage());
            DialogUtils.showException(QiniuConsts.UPDATE_ERROR, e);
        }
    }

    /**
     * 公有下载
     */
    public void publicDownload(String fileName, String domain) {
        QiniuUtils.download(QiniuUtils.buildUrl(fileName, domain));
    }

    /**
     * 私有下载
     */
    public void privateDownload(String fileName, String domain) {
        QiniuUtils.download(sdkManager.getPrivateUrl(QiniuUtils.buildUrl(fileName, domain)));
    }

    /**
     * 刷新文件
     */
    public void refreshFile(ObservableList<FileBean> fileBeans, String domain) {
        if (CollUtil.isNotEmpty(fileBeans)) {
            String[] files = new String[fileBeans.size()];
            int i = 0;
            // 获取公有链接
            for (FileBean fileBean : fileBeans) {
                files[i++] = QiniuUtils.buildUrl(fileBean.getName(), domain);
            }
            try {
                // 刷新文件
                sdkManager.refreshFile(files);
            } catch (QiniuException e) {
                log.error("refresh files error, message -> " + e.getMessage());
                DialogUtils.showException(e);
            }
        }
    }

    /**
     * 日志下载
     */
    public void downloadCdnLog(String logDate) {
        if (CollUtil.isNotEmpty(ConfigBean.getConfig().getBuckets()) && QiniuUtils.isDate(logDate)) {
            // 转换域名成数组格式
            String[] domains = new String[ConfigBean.getConfig().getBuckets().size()];
            for (int i = 0; i < ConfigBean.getConfig().getBuckets().size(); i++) {
                domains[i] = ConfigBean.getConfig().getBuckets().get(i).getUrl();
            }
            Map<String, CdnResult.LogData[]> cdnLog = null;
            try {
                cdnLog = sdkManager.listCdnLog(domains, logDate);
            } catch (QiniuException e) {
                DialogUtils.showException(e);
            }
            if (CollUtil.isNotEmpty(cdnLog)) {
                // 下载日志
                for (Map.Entry<String, CdnResult.LogData[]> logs : cdnLog.entrySet()) {
                    for (CdnResult.LogData log : logs.getValue()) {
                        QiniuUtils.download(log.url);
                    }
                }
            }
        }
    }


    /**
     * 获取空间带宽统计，使用自定义单位
     */
    public XYChart.Series<String, Long> getBucketBandwidth(String[] domains, String startDate, String endDate,
                                                           String unit) {
        // 获取带宽数据
        CdnResult.BandwidthResult bandwidthResult = null;
        try {
            bandwidthResult = sdkManager.getBandwidthData(domains, startDate, endDate);
        } catch (QiniuException e) {
            Platform.runLater(() -> DialogUtils.showException(QiniuConsts.BUCKET_BAND_ERROR, e));
        }
        // 设置图表
        XYChart.Series<String, Long> series = new XYChart.Series<>();
        series.setName(QiniuConsts.BUCKET_BANDWIDTH_COUNT.replaceAll("[A-Z]+", unit));
        // 格式化数据
        if (ObjectUtil.isNotNull(bandwidthResult) && CollUtil.isNotEmpty(bandwidthResult.data)) {
            long unitSize = QiniuUtils.sizeToLong("1 " + unit);
            for (Map.Entry<String, CdnResult.BandwidthData> bandwidth : bandwidthResult.data.entrySet()) {
                CdnResult.BandwidthData bandwidthData = bandwidth.getValue();
                if (ObjectUtil.isNotNull(bandwidthData)) {
                    setSeries(bandwidthResult.time, bandwidthData.china, bandwidthData.oversea, series, unitSize);
                }
            }
        }
        return series;
    }

    /**
     * 获取空间的流量统计，使用自定义单位
     */
    public XYChart.Series<String, Long> getBucketFlux(String[] domains, String startDate, String endDate, String unit) {
        // 获取流量数据
        CdnResult.FluxResult fluxResult = null;
        try {
            fluxResult = sdkManager.getFluxData(domains, startDate, endDate);
        } catch (QiniuException e) {
            Platform.runLater(() -> DialogUtils.showException(QiniuConsts.BUCKET_FLUX_ERROR, e));
        }
        // 设置图表
        XYChart.Series<String, Long> series = new XYChart.Series<>();
        series.setName(QiniuConsts.BUCKET_FLUX_COUNT.replaceAll("[A-Z]+", unit));
        // 格式化数据
        if (ObjectUtil.isNotNull(fluxResult) && CollUtil.isNotEmpty(fluxResult.data)) {
            long unitSize = QiniuUtils.sizeToLong("1 " + unit);
            for (Map.Entry<String, CdnResult.FluxData> flux : fluxResult.data.entrySet()) {
                CdnResult.FluxData fluxData = flux.getValue();
                if (ObjectUtil.isNotNull(fluxData)) {
                    setSeries(fluxResult.time, fluxData.china, fluxData.oversea, series, unitSize);
                }
            }
        }
        return series;
    }

    /**
     * 处理带宽数据
     */
    private void setSeries(String[] times, Long[] china, Long[] oversea, XYChart.Series<String, Long> series,
                           long unit) {
        int i = 0;
        for (String time : times) {
            long size = 0;
            if (ArrayUtil.isNotEmpty(china)) {
                size += china[i];
            }
            if (ArrayUtil.isNotEmpty(oversea)) {
                size += oversea[i];
            }
            series.getData().add(new XYChart.Data<>(time.substring(5, 10), size / unit));
            i++;
        }
    }
}
