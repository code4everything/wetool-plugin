package org.code4everything.wetool.plugin.everywhere.constant;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import lombok.experimental.UtilityClass;

import java.io.File;

/**
 * @author pantao
 * @since 2019/11/26
 */
@UtilityClass
public class CommonConsts {

    public static final String APP_ID = "ease-everywhere";

    public static final String APP_NAME = "Find In Everywhere";

    public static final String INDEX_PATH = StrUtil.join(File.separator, FileUtil.getUserHomePath(), "wetool",
            "wetool-plugin-everywhere", ".lucene");

    /**
     * 创建内容索引的文件最大大小
     */
    public static final int MAX_FILE_SIZE = 10_000_000;
}
