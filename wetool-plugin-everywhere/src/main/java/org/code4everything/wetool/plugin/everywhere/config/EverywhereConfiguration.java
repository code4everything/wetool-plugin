package org.code4everything.wetool.plugin.everywhere.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.code4everything.boot.base.bean.BaseBean;

import java.util.Set;

/**
 * @author pantao
 * @since 2019/11/26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class EverywhereConfiguration implements BaseBean {

    private Set<String> excludeFileRegex;

    private Set<String> indexFileRegex;
}
