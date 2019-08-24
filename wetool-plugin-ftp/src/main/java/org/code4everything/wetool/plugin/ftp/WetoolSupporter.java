package org.code4everything.wetool.plugin.ftp;

import org.code4everything.wetool.plugin.support.WePluginSupportable;

/**
 * @author pantao
 * @since 2019/8/23
 */
public class WetoolSupporter implements WePluginSupportable {

    @Override
    public void initialize() {
        System.out.println("init ftp");
    }
}
