package org.code4everything.wetool.plugin.devtool.redis.config;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.code4everything.boot.base.bean.BaseBean;

/**
 * @author pantao
 * @since 2020/9/21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class GeoAmapConfiguration implements BaseBean {

    private Boolean circleEnabled;

    private Integer radius;

    private String strokeColor;

    private Double strokeOpacity;

    private Integer strokeWeight;

    private String fillColor;

    private Double fillOpacity;

    public Integer getRadius() {
        return ObjectUtil.defaultIfNull(radius, 1500);
    }

    public String getStrokeColor() {
        return StrUtil.blankToDefault(strokeColor, "blue");
    }

    public Double getStrokeOpacity() {
        return ObjectUtil.defaultIfNull(strokeOpacity, 0.15);
    }

    public Integer getStrokeWeight() {
        return ObjectUtil.defaultIfNull(strokeWeight, 1);
    }

    public String getFillColor() {
        return StrUtil.blankToDefault(fillColor, "blue");
    }

    public Double getFillOpacity() {
        return ObjectUtil.defaultIfNull(fillOpacity, 0.15);
    }
}
