package org.code4everything.wetool.plugin.qiniu.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import javafx.application.Platform;
import lombok.experimental.UtilityClass;
import org.code4everything.wetool.plugin.qiniu.api.SdkConfigurer;
import org.code4everything.wetool.plugin.qiniu.constant.QiniuConsts;
import org.code4everything.wetool.plugin.qiniu.controller.MainController;
import org.code4everything.wetool.plugin.qiniu.model.BucketBean;
import org.code4everything.wetool.plugin.qiniu.model.ConfigBean;
import org.code4everything.wetool.plugin.support.factory.BeanFactory;

import java.util.ArrayList;

/**
 * 配置文件工具类
 *
 * @author pantao
 * @since 2018/11/12
 **/
@UtilityClass
public class ConfigUtils {

    private static final String CONFIG_PATH = StrUtil.emptyToDefault(QiniuConsts.CONFIG_PATH, QiniuConsts.DEFAULT_PATH);

    /**
     * 加载配置文件
     */
    public static void loadConfig() {
        if (FileUtil.exist(CONFIG_PATH)) {
            ConfigBean config = JSON.parseObject(FileUtil.readUtf8String(CONFIG_PATH), ConfigBean.class);
            if (ObjectUtil.isNotNull(config)) {
                BeanFactory.register(config);
                if (StrUtil.isNotEmpty(config.getAccessKey()) && StrUtil.isNotEmpty(config.getSecretKey())) {
                    // 创建上传权限
                    SdkConfigurer.createAuth(config.getAccessKey(), config.getSecretKey());
                }
                MainController controller = MainController.getInstance();
                if (CollUtil.isEmpty(config.getBuckets())) {
                    // 设置一个空的桶列表，防止出现空指针
                    config.setBuckets(new ArrayList<>());
                } else {
                    Platform.runLater(() -> {
                        // 添加桶
                        config.getBuckets().forEach(bucket -> controller.appendBucket(bucket.getBucket()));
                        // 选中第一个桶
                        BucketBean bucket = config.getBuckets().get(0);
                        controller.bucketCB.setValue(bucket.getBucket());
                        controller.zoneTF.setText(bucket.getZone());
                    });
                }
                if (CollUtil.isEmpty(config.getPrefixes())) {
                    // 设置一个空的前缀列表，防止出现空指针
                    config.setPrefixes(new ArrayList<>());
                } else {
                    // 添加前缀
                    Platform.runLater(() -> config.getPrefixes().forEach(prefix -> controller.prefixCB.getItems().add(prefix)));
                }
                return;
            }
        }
        // 设置一个空的配置对象，防止出现空指针
        ConfigBean configBean = new ConfigBean();
        configBean.setPrefixes(new ArrayList<>());
        configBean.setBuckets(new ArrayList<>());
        BeanFactory.register(configBean);
        writeConfig();
    }

    /**
     * 写配置文件
     */
    public static void writeConfig() {
        FileUtil.writeUtf8String(JSON.toJSONString(ConfigBean.getConfig(), true), CONFIG_PATH);
    }
}
