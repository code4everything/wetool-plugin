package org.code4everything.wetool.plugin.everywhere.util;

import cn.hutool.core.lang.Console;
import org.junit.Test;

import java.io.IOException;

public class FileTypeUtilsTest {

    @Test
    public void isTextFile() throws IOException {
        Console.log(FileTypeUtils.isTextFile("D:\\Project\\Java\\wetool\\src\\main\\java\\org\\code4everything\\wetool\\WeApplication.java"));
    }
}
