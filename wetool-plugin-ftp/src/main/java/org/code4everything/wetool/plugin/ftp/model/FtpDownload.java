package org.code4everything.wetool.plugin.ftp.model;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.net.ftp.FTPFile;
import org.code4everything.boot.base.bean.BaseBean;
import org.code4everything.boot.base.constant.StringConsts;

import java.io.File;
import java.io.Serializable;

/**
 * @author pantao
 * @since 2019/8/25
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FtpDownload implements BaseBean, Serializable {

    private static final long serialVersionUID = 8142935768386723746L;

    private String name;

    private String file;

    private Boolean directory;

    private File path;

    public static FtpDownload childDownload(FtpDownload parent, FTPFile child) {
        if (!parent.directory) {
            return parent;
        }
        // 子文件全路径名
        String filename = StrUtil.addSuffixIfNot(parent.getFile(), "/") + child.getName();
        // 本地保存路径
        File newPath;
        if (StrUtil.isEmpty(parent.getFile()) || StringConsts.Sign.SLASH.equals(parent.getFile())) {
            newPath = parent.getPath();
        } else {
            // 解析parent的文件名，获取新的保存路径
            String absPath = StrUtil.addPrefixIfNot(StrUtil.removeSuffix(parent.getFile(), "/"), "/");
            String parentFolder = parent.getPath().getAbsolutePath() + File.separator;
            newPath = new File(parentFolder + absPath.substring(absPath.lastIndexOf("/") + 1));
            newPath.mkdirs();
        }
        return new FtpDownload(parent.getName(), filename, child.isDirectory(), newPath);
    }
}
