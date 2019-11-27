package org.code4everything.wetool.plugin.everywhere;

import cn.hutool.core.lang.Console;
import org.junit.Test;

import java.io.File;

/**
 * @author pantao
 * @since 2019/11/27
 */
public class CommonTest {

    @Test
    public void testFileRoots() {
        File[] fs = File.listRoots();
        for (File f : fs) {
            Console.log(f.listFiles());
        }
    }
}
