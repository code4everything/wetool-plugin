package org.code4everything.wetool.plugin.test;

import org.code4everything.wetool.plugin.support.config.WePluginInfo;

class WetoolTesterTest {

    public static void main(String[] args) {
        WePluginInfo info = new WePluginInfo("ease", "test", "1.3.0", "1.3.0", "");
        info.setSupportedClass("org.code4everything.wetool.plugin.test.WetoolSupporter");
        WetoolTester.runTest(info, args);
    }
}
