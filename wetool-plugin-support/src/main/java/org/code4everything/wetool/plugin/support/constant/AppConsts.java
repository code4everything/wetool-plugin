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

    /**
     * 当前版本号
     */
    public static final String CURRENT_VERSION = "1.3.0";

    /**
     * 兼容的最低版本号
     */
    public static final String COMPATIBLE_LOWER_VERSION = "1.1.0";

    public static final String DARK_CSS = "/css/dark.css";

    @UtilityClass
    public static class Tip {

        public static final String OPEN_FILE_ERROR = "打开文件失败";

        public static final String OPERATION_SUCCESS = "操作成功";

        public static final String OPEN_LINK_ERROR = "打开链接失败";

        public static final String FXML_ERROR = "加载视图失败";
    }

    @UtilityClass
    public static class Title {

        public static final String APP_TITLE = "WeTool";

        public static final String DEV_TOOL = "开发工具";
    }

    @UtilityClass
    public static class BeanKey {

        public static final String PLUGIN_MENU = "pluginMenu";

        public static final String TITLE_BAR = "titleBar";
    }
}
