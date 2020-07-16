package org.code4everything.wetool.plugin.devtool.ssh.config;

import cn.hutool.core.util.StrUtil;
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

    private boolean isDir;

    public static SftpFile of(SftpFile parent, String path, boolean isDir) {
        return new SftpFile((parent.isDir ? parent.getPath() : "") + path, isDir);
    }

    public String getPath() {
        if (!isDir) {
            return path;
        }
        path = StrUtil.emptyToDefault(path, "/");
        return StrUtil.addSuffixIfNot(path, "/");
    }

    @Override
    public String toString() {
        return getPath();
    }
}
