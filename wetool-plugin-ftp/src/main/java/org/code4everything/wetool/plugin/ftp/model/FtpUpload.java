package org.code4everything.wetool.plugin.ftp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.code4everything.boot.base.bean.BaseBean;

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
public class FtpUpload implements BaseBean, Serializable {

    private static final long serialVersionUID = 553115546970154169L;

    private String name;

    private String path;

    private File file;
}
