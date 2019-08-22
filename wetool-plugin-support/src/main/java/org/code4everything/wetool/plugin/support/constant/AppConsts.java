package org.code4everything.wetool.plugin.support.constant;

import lombok.experimental.UtilityClass;

/**
 * 通用常量类
 *
 * @author pantao
 * @since 2019/8/22
 */
@UtilityClass
public class AppConsts {

    @UtilityClass
    public static class Tip {

        public static final String OPEN_FILE_ERROR = "打开文件失败";

        public static final String OPERATION_SUCCESS = "操作成功";

        public static final String OPEN_LINK_ERROR = "打开链接失败";

        public static final String FXML_ERROR = "加载视图失败";
    }

    @UtilityClass
    public static class Title {

        public static final String APP_TITLE = "工具集";
    }
}
