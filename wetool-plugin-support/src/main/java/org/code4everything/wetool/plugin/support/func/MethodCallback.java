package org.code4everything.wetool.plugin.support.func;

import com.google.common.base.Preconditions;
import org.code4everything.wetool.plugin.support.druid.JdbcOpsUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author pantao
 * @since 2021/1/16
 */
public interface MethodCallback {

    /**
     * 自定义方法全局唯一名称
     *
     * @return 全局唯一名称
     */
    String getUniqueMethodName();

    /**
     * 方法所需参数类型
     *
     * @return 参数类型列表
     */
    List<Class<?>> getParamTypes();

    /**
     * 调用方法
     *
     * @param params 方法参数
     *
     * @return 执行结果
     */
    Object callMethod(List<Object> params);

    /**
     * 检查参数类型并调用方法
     *
     * @param params 方法参数
     *
     * @return 执行结果
     */
    default Object checkAndCallMethod(List<Object> params) {
        Objects.requireNonNull(params);
        List<Class<?>> paramTypes = getParamTypes();
        Preconditions.checkArgument(paramTypes.size() == params.size(), "list size inconsistency");

        List<Object> checkedParam = new ArrayList<>(params.size());
        for (int i = 0; i < params.size(); i++) {
            checkedParam.add(JdbcOpsUtils.fastCast(params.get(i), paramTypes.get(i)));
        }

        return callMethod(checkedParam);
    }
}
