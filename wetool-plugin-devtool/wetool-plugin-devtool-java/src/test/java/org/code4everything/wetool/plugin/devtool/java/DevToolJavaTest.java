package org.code4everything.wetool.plugin.devtool.java;

import org.code4everything.boot.base.FileUtils;
import org.code4everything.wetool.plugin.support.config.WeConfig;
import org.code4everything.wetool.plugin.test.WetoolTester;

/**
 * @author pantao
 * @since 2019/9/26
 */
public class DevToolJavaTest {

    public static void main(String[] args) {
        WeConfig config = WetoolTester.getConfig();
        config.setFileChooserInitDir(FileUtils.currentWorkDir());
        WetoolTester.runTest(config, args);
    }
}
