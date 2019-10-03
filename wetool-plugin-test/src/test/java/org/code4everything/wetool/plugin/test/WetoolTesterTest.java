package org.code4everything.wetool.plugin.test;

import javafx.scene.control.MenuItem;
import org.code4everything.wetool.plugin.support.WePluginSupporter;
import org.code4everything.wetool.plugin.support.config.WePluginInfo;
import org.code4everything.wetool.plugin.support.constant.AppConsts;
import org.code4everything.wetool.plugin.support.util.FxDialogs;

class WetoolTesterTest {

    public static void main(String[] args) {
        WePluginInfo info = new WePluginInfo("ease", "test", "1.0.2", "1.0.2", "");
        WePluginSupporter supporter = new WePluginSupporter() {
            @Override
            public MenuItem registerBarMenu() {
                FxDialogs.showInformation(AppConsts.Title.APP_TITLE, "Test Ok");
                return null;
            }
        };
        WetoolTester.runTest(info, supporter, args);
    }
}
