package org.code4everything.wetool.plugin.test;

import javafx.scene.control.MenuItem;
import org.code4everything.wetool.plugin.support.WePluginSupporter;
import org.code4everything.wetool.plugin.support.constant.AppConsts;
import org.code4everything.wetool.plugin.support.util.FxDialogs;

/**
 * @author pantao
 * @since 2019/10/8
 */
public class WetoolSupporter implements WePluginSupporter {

    @Override
    public MenuItem registerBarMenu() {
        FxDialogs.showInformation(AppConsts.Title.APP_TITLE, "Test Ok");
        return null;
    }
}
