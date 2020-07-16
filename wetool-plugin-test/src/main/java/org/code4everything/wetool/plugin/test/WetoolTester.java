package org.code4everything.wetool.plugin.test;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.system.SystemUtil;
import com.alibaba.fastjson.JSON;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.code4everything.boot.config.BootConfig;
import org.code4everything.wetool.WeApplication;
import org.code4everything.wetool.plugin.PluginLoader;
import org.code4everything.wetool.plugin.support.WePluginSupporter;
import org.code4everything.wetool.plugin.support.config.WeConfig;
import org.code4everything.wetool.plugin.support.config.WeInitialize;
import org.code4everything.wetool.plugin.support.config.WePluginInfo;
import org.code4everything.wetool.plugin.support.config.WeTab;
import org.code4everything.wetool.plugin.support.factory.BeanFactory;

import java.util.HashSet;
import java.util.Objects;

/**
 * @author pantao
 * @since 2019/8/23
 */
@Slf4j
public class WetoolTester extends WeApplication {

    private static WePluginSupporter supporter;

    private static WePluginInfo info;

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
        // 加载插件支持类
        BootConfig.setDebug(true);
        Class<WePluginSupporter> clazz = ClassUtil.loadClass(info.getSupportedClass());
        try {
            supporter = clazz.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        // 启动WeTool
        log.info("starting wetool on os: {}", SystemUtil.getOsInfo().getName());
        WetoolTester.info = info;
        BeanFactory.register(config);
        launch(args);
    }

    public static WeConfig getConfig() {
        WeConfig config = new WeConfig();
        config.setAutoWrap(true);
        config.setClipboardSize(20);
        config.setFileFilter("^[^.].*$");
        config.setQuickStarts(new HashSet<>());
        config.setPluginDisables(new HashSet<>());

        WeInitialize initialize = new WeInitialize();
        initialize.setFullscreen(false);
        initialize.setHeight(700);
        initialize.setHide(false);
        initialize.setWidth(1000);

        WeTab tab = new WeTab();
        tab.setLoads(new HashSet<>());
        tab.setSupports(new HashSet<>());

        initialize.setTabs(tab);
        config.setInitialize(initialize);
        return config;
    }

    @Override
    public void start(Stage stage) {
        super.start(stage);
        Objects.requireNonNull(info);
        Objects.requireNonNull(supporter);
        PluginLoader.registerPlugin(info, supporter);
    }
}
