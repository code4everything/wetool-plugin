package org.code4everything.wetool.plugin.ftp.server;

import cn.hutool.core.lang.Console;
import cn.hutool.extra.ftp.Ftp;
import org.junit.Test;

/**
 * @author pantao
 * @since 2019/9/23
 */
public class FtpServerConnectionTest {

    @Test
    public void testConnection() {
        try {
            Ftp ftp = new Ftp("127.0.0.1", 21, "test", "test");
            Console.log(ftp.ls("/"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
