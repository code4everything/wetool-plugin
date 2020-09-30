package org.code4everything.wetool.plugin.test;

import org.code4everything.wetool.plugin.support.config.WePluginInfo;

class WetoolTesterTest {

    public static void main(String[] args) {
        WePluginInfo info = new WePluginInfo("ease", "test", "1.1.2", "1.1.2", "");
        info.setSupportedClass("org.code4everything.wetool.plugin.test.WetoolSupporter");
        WetoolTester.runTest(info, args);
    }
}
