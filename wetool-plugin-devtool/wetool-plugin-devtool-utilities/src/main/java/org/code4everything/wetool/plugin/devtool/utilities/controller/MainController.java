package org.code4everything.wetool.plugin.devtool.utilities.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.thread.ThreadFactoryBuilder;
import cn.hutool.core.util.NumberUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.code4everything.wetool.plugin.devtool.utilities.constant.CommonConsts;
import org.code4everything.wetool.plugin.support.BaseViewController;
import org.code4everything.wetool.plugin.support.factory.BeanFactory;
import org.code4everything.wetool.plugin.support.util.FxDialogs;
import org.code4everything.wetool.plugin.support.util.FxUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * @author pantao
 * @since 2019/11/4
 */
public class MainController implements BaseViewController {

    private static final ThreadFactory FACTORY = ThreadFactoryBuilder.create().setDaemon(true).build();

    private static final ScheduledThreadPoolExecutor EXECUTOR = new ScheduledThreadPoolExecutor(1, FACTORY);

    private final Map<Boolean, String> toggleMap = new HashMap<>(2, 1);

    @FXML
    public TextField timestampField;

    @FXML
    public TextField currentTimestampField;

    @FXML
    public TextField dateTimeField;

    @FXML
    public TextField currentDateTimeField;

    @FXML
    public Button toggleButton;

    private boolean shouldUpdateTime = false;

    private Date date = new Date();

    @FXML
    public void initialize() {
        toggleMap.put(true, "开始");
        toggleMap.put(false, "停止");
        toggleScheduler();
        BeanFactory.registerView(CommonConsts.APP_ID, CommonConsts.APP_NAME, this);
        EXECUTOR.scheduleWithFixedDelay(this::updateTime, 0, 1000, TimeUnit.MILLISECONDS);
    }

    private void updateTime() {
        if (!shouldUpdateTime || !FxUtils.getStage().isShowing() || this != FxUtils.getSelectedTabController()) {
            return;
        }
        date.setTime(System.currentTimeMillis());
        currentDateTimeField.setText(DateUtil.formatDateTime(date));
        currentTimestampField.setText(String.valueOf(date.getTime()));
    }

    public void toggleScheduler() {
        toggleButton.setText(toggleMap.get(shouldUpdateTime));
        shouldUpdateTime = !shouldUpdateTime;
    }

    public void toTimestamp() {
        DateTime dateTime;
        try {
            dateTime = DateUtil.parseDateTime(dateTimeField.getText());
        } catch (Exception e) {
            FxDialogs.showError("解析日期格式错误，请输入标准日期格式！");
            return;
        }
        timestampField.setText(String.valueOf(dateTime.getTime()));
    }

    public void toDateTime() {
        try {
            long timestamp = NumberUtil.parseLong(timestampField.getText());
            dateTimeField.setText(DateUtil.formatDateTime(new Date(timestamp)));
        } catch (Exception e) {
            FxDialogs.showError("请输入正确的时间戳格式！");
        }
    }
}
