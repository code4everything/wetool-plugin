package org.code4everything.wetool.plugin.support.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.*;
import org.code4everything.boot.base.bean.BaseBean;

import java.io.Serializable;
import java.util.Set;

/**
 * @author pantao
 * @since 2019/8/21
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"location", "subStarts"})
public class WeStart implements BaseBean, Serializable {

    private static final long serialVersionUID = 762565001230119596L;

    /**
     * 菜单名称
     */
    private String alias;

    /**
     * 文件的路径，不包含子菜单时此选项可生效
     */
    private String location;

    /**
     * 子菜单，如配置了此属性，说明当前的菜单是一个父级菜单
     */
    private Set<WeStart> subStarts;

    @Override
    public void requireNonNullProperty() {
        String msg = "the value of field '%s' at class '" + getClass().getName() + "' must not be empty";
        if (StrUtil.isEmpty(alias)) {
            throw new NullPointerException(String.format(msg, "alias"));
        }
        if (CollUtil.isNotEmpty(subStarts)) {
            subStarts.forEach(WeStart::requireNonNullProperty);
        } else if (StrUtil.isEmpty(location)) {
            throw new NullPointerException(String.format(msg, "location"));
        }
    }
}
