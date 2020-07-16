package org.code4everything.wetool.plugin.everywhere.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.code4everything.boot.base.bean.BaseBean;

/**
 * @author pantao
 * @since 2019/11/27
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class FileInfo implements BaseBean {

    private String filename;

    private String path;

    private String modified;

    private String size;
}
