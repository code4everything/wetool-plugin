package org.code4everything.wetool.plugin.everywhere.filter;

import java.io.File;
import java.util.Objects;

/**
 * @author pantao
 * @since 2019/11/26
 */
public class FilenameFilter implements IndexFilter {

    @Override
    public boolean shouldIndex(File file) {
        if (Objects.isNull(file)) {
            return false;
        }
        return !file.isDirectory();
    }
}
