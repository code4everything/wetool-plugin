package org.code4everything.wetool.plugin.qiniu.api;

import com.qiniu.cdn.CdnManager;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.persistent.FileRecorder;
import com.qiniu.util.Auth;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.code4everything.boot.base.FileUtils;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * @author pantao
 */
@Slf4j
@UtilityClass
public class SdkConfigurer {

    private static final String[] BUCKET_NAME_ARRAY = {"华东", "华北", "华南", "北美"};

    private static final Map<String, Region> REGION_MAP = new HashMap<>();

    private static Auth auth = null;

    private static UploadManager uploadManager = null;

    private static BucketManager bucketManager = null;

    private static CdnManager cdnManager = null;

    static {
        // 加载空间区域
        REGION_MAP.put(BUCKET_NAME_ARRAY[0], Region.huadong());
        REGION_MAP.put(BUCKET_NAME_ARRAY[1], Region.huabei());
        REGION_MAP.put(BUCKET_NAME_ARRAY[2], Region.huanan());
        REGION_MAP.put(BUCKET_NAME_ARRAY[3], Region.beimei());
    }

    public static CdnManager getCdnManager() {
        return cdnManager;
    }

    public static BucketManager getBucketManager() {
        return bucketManager;
    }

    public static UploadManager getUploadManager() {
        return uploadManager;
    }

    public static Auth getAuth() {
        return auth;
    }

    /**
     * 创建上传需要的Auth
     */
    public static void createAuth(String accessKey, String secretKey) {
        auth = Auth.create(accessKey, secretKey);
        cdnManager = new CdnManager(auth);
    }

    /**
     * 配置文件上传环境，不再做网络检查，请执行保证网络通畅
     */
    public static boolean configUploadEnv(String zone, String bucket) {
        // 构造一个带指定Zone对象的配置类
        Configuration configuration = new Configuration(SdkConfigurer.REGION_MAP.get(zone));
        // 生成上传凭证，然后准备上传
        String workDir = Paths.get(FileUtils.currentWorkDir(), bucket).toString();
        try {
            FileRecorder fileRecorder = new FileRecorder(workDir);
            uploadManager = new UploadManager(configuration, fileRecorder);
        } catch (IOException e) {
            log.warn("load work directory failed, can't use file recorder");
            uploadManager = new UploadManager(configuration);
        }
        bucketManager = new BucketManager(auth, configuration);
        return true;
    }
}
