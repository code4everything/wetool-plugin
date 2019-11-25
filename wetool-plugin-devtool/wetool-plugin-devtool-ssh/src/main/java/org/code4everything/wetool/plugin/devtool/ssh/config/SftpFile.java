package org.code4everything.wetool.plugin.devtool.ssh.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.code4everything.boot.base.bean.BaseBean;

/**
 * @author pantao
 * @since 2019/11/25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class SftpFile implements BaseBean {

    private String path;

    private Boolean isDir;

    @Override
    public String toString() {
        return path;
    }
}
