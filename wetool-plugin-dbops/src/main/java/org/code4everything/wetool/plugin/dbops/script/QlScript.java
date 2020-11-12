package org.code4everything.wetool.plugin.dbops.script;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author pantao
 * @since 2020/11/11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class QlScript {

    /**
     * 唯一标识，系统生成
     */
    private String uuid;

    /**
     * 名称
     */
    private String name;

    /**
     * 备注
     */
    private String comment;

    /**
     * 触发类型
     */
    private ExecuteTypeEnum type;

    /**
     * 如果是由事件触发，此字段必填
     */
    private String eventKey;

    /**
     * 指定数据源执行，否则使用当前用户选择的数据源
     */
    private String specifyDbName;

    /**
     * 代码块
     */
    private String codes;
}
