package org.code4everything.wetool.plugin.test;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.system.SystemUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.parser.Feature;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.code4everything.boot.config.BootConfig;
import org.code4everything.wetool.WeApplication;
import org.code4everything.wetool.plugin.PluginLoader;
import org.code4everything.wetool.plugin.support.config.WeConfig;
import org.code4everything.wetool.plugin.support.config.WeInitialize;
import org.code4everything.wetool.plugin.support.config.WePluginInfo;
import org.code4everything.wetool.plugin.support.config.WeStatus;
import org.code4everything.wetool.plugin.support.config.WeTab;
import org.code4everything.wetool.plugin.support.event.message.MouseCornerEventMessage;
import org.code4everything.wetool.plugin.support.factory.BeanFactory;
import org.code4everything.wetool.plugin.support.http.HttpService;
import org.code4everything.wetool.plugin.support.util.WeUtils;

import java.util.HashSet;
import java.util.Objects;

/**
 * @author pantao
 * @since 2019/8/23
 */
@Slf4j
public class WetoolTester extends WeApplication {

    private static WePluginInfo info;

    private static WeConfig weConfig;

    public static void runTest(String[] args) {
        runTest(getConfig(), args);
    }

    public static void runTest(WeConfig config, String[] args) {
        String json = IoUtil.read(WetoolTester.class.getResourceAsStream("/plugin.json"), "utf-8");
        runTest(JSON.parseObject(json, WePluginInfo.class), config, args);
    }

    public static void runTest(WePluginInfo info, String[] args) {
        runTest(info, getConfig(), args);
    }

    public static void runTest(WePluginInfo info, WeConfig config, String[] args) {
        BeanFactory.register(new WeStatus().setState(WeStatus.State.STARTING));
        BootConfig.setDebug(true);
        HttpService.setDefaultPort(58189);

        // 启动WeTool
        log.info("starting wetool on os: {}", SystemUtil.getOsInfo().getName());
        WetoolTester.info = info;
        BeanFactory.register(config);
        WeApplication.initApp();
        launch(args);
    }

    public static WeConfig getConfig() {
        if (Objects.nonNull(weConfig)) {
            return weConfig;
        }

        weConfig = new WeConfig();
        weConfig.setAutoWrap(true);
        weConfig.setClipboardSize(20);
        weConfig.setFileFilter("^[^.].*$");
        weConfig.setQuickStarts(new HashSet<>());
        weConfig.setPluginDisables(new HashSet<>());
        weConfig.setDbConnections(new JSONArray());
        weConfig.setWinVirtualDesktopHotCorner(MouseCornerEventMessage.LocationTypeEnum.NONE);

        WeInitialize initialize = new WeInitialize();
        initialize.setFullscreen(false);
        initialize.setHeight(700);
        initialize.setHide(false);
        initialize.setWidth(1000);

        WeTab tab = new WeTab();
        tab.setLoads(new HashSet<>());
        tab.setSupports(new HashSet<>());

        initialize.setTabs(tab);
        weConfig.setInitialize(initialize);
        return weConfig;
    }

    public static void setConfig(String jsonPath) {
        setConfig(JSON.parseObject(FileUtil.readUtf8String(jsonPath), WeConfig.class, Feature.OrderedField));
    }

    public static void setConfig(WeConfig weConfig) {
        WetoolTester.weConfig = weConfig;
    }

    @Override
    public void start(Stage stage) {
        super.start(stage);
        Objects.requireNonNull(info);
        WeUtils.execute(() -> PluginLoader.loadPluginForTest(info));
    }
}
