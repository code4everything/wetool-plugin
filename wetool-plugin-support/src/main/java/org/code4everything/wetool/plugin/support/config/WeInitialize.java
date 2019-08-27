package org.code4everything.wetool.plugin.support.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.code4everything.boot.base.bean.BaseBean;

import java.io.Serializable;

/**
 * @author pantao
 * @since 2019/7/3
 **/
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class WeInitialize implements BaseBean, Serializable {

    private static final long serialVersionUID = -3706972162680878384L;

    /**
     * 初始化宽度
     */
    private Integer width;

    /**
     * 初始化高度
     */
    private Integer height;

    /**
     * 初始化时是否全屏
     */
    private Boolean fullscreen;

    /**
     * 初始时是否自动隐藏
     */
    private Boolean hide;

    /**
     * 初始加载的选项卡
     */
    private WeTab tabs;
}
