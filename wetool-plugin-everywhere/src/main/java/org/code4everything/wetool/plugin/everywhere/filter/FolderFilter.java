package org.code4everything.wetool.plugin.everywhere.filter;

import org.code4everything.wetool.plugin.everywhere.config.EverywhereConfiguration;
import org.code4everything.wetool.plugin.everywhere.constant.CommonConsts;

import java.io.File;
import java.util.Objects;

/**
 * @author pantao
 * @since 2019/11/26
 */
public class FolderFilter implements IndexFilter {

    @Override
    public boolean shouldIndex(File file) {
        if (Objects.isNull(file)) {
            return false;
        }
        if (file.isFile()) {
            return false;
        }
        EverywhereConfiguration configuration = EverywhereConfiguration.getFormatted();
        if (configuration.isIgnoreHiddenFile() && file.isHidden()) {
            return false;
        }
        if (CommonConsts.INDEX_PATH.equals(file.getAbsolutePath())) {
            return false;
        }
        return true;
    }
}
