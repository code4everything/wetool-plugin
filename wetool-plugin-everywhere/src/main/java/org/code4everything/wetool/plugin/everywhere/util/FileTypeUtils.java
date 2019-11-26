package org.code4everything.wetool.plugin.everywhere.util;

import lombok.Cleanup;
import lombok.experimental.UtilityClass;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author pantao
 * @since 2019/11/26
 */
@UtilityClass
public class FileTypeUtils {

    public static boolean isTextFile(String filename) throws IOException {
        @Cleanup InputStream inputStream = new FileInputStream(filename);
        byte[] bytes = new byte[500];
        inputStream.read(bytes, 0, bytes.length);
        short bin = 0;
        for (byte thisByte : bytes) {
            char it = (char) thisByte;
            if (!Character.isWhitespace(it) && Character.isISOControl(it)) {
                bin++;
            }
            if (bin >= 5) {
                return false;
            }
        }
        return true;
    }
}
