package org.code4everything.wetool.plugin.test;

import javafx.scene.control.MenuItem;
import org.code4everything.wetool.plugin.support.WePluginSupportable;
import org.code4everything.wetool.plugin.support.config.WePluginInfo;
import org.code4everything.wetool.plugin.support.constant.AppConsts;
import org.code4everything.wetool.plugin.support.util.FxDialogs;

class WetoolTestTester {

    public static void main(String[] args) {
        WePluginInfo info = new WePluginInfo("ease", "test", "1.0.0");
        WePluginSupportable supportable = new WePluginSupportable() {
            @Override
            public MenuItem registerBarMenu() {
                FxDialogs.showInformation(AppConsts.Title.APP_TITLE, "Test Ok");
                return null;
            }
        };
        WetoolTest.runTest(info, supportable, args);
    }
}
