package org.code4everything.wetool.plugin.ftp.server;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.extra.ftp.Ftp;
import org.junit.Test;

import java.util.List;

/**
 * @author pantao
 * @since 2019/9/23
 */
public class FtpServerConnectionTest {

    @Test
    public void testConnection() {
        Ftp ftp = new Ftp("127.0.0.1", 21, "test", "test");
        List<String> ls = ftp.ls("/");
        System.out.println(ls);
        assert CollUtil.isNotEmpty(ls);
    }
}
