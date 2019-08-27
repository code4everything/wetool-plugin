package org.code4everything.wetool.plugin.support.config;

import lombok.*;
import org.code4everything.boot.base.bean.BaseBean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author pantao
 * @since 2019/7/3
 **/
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class WeTab implements BaseBean, Serializable {

    private static final long serialVersionUID = -7095575648923571810L;

    /**
     * 初始化加载的选项卡
     */
    private List<String> loads;

    /**
     * 支持加载的选项卡，可不配置此属性，展示的配置文件只是为了说明目前支持的选项卡
     */
    private List<String> supports;

    @Override
    public void requireNonNullProperty() {}

    @Generated
    public List<String> getLoads() {
        return Objects.isNull(loads) ? new ArrayList<>() : loads;
    }
}
