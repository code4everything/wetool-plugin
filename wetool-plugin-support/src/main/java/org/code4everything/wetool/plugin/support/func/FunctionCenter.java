package org.code4everything.wetool.plugin.support.func;

import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 方法的注册与调用
 * <p>
 * 注：不使用反射，因为java11中反射权限需要模块显示开启，如使用反射可能导致无法调用
 *
 * @author pantao
 * @since 2021/1/16
 */
@UtilityClass
public class FunctionCenter {

    private static final Map<String, MethodCallback> METHOD_CALLBACK_MAP = new ConcurrentHashMap<>();

    /**
     * 注册可调用的方法
     *
     * @param methodCallback 方法回调
     *
     * @return 是否注册成功
     */
    public static boolean registerFunc(MethodCallback methodCallback) {
        Objects.requireNonNull(methodCallback);
        if (METHOD_CALLBACK_MAP.containsKey(methodCallback.getUniqueMethodName())) {
            return false;
        }
        METHOD_CALLBACK_MAP.put(methodCallback.getUniqueMethodName(), methodCallback);
        return true;
    }

    /**
     * 调用方法
     *
     * @param uniqueMethodName 方法全局唯一名称
     * @param params 调用参数
     *
     * @return 执行结果
     */
    public static Object callFunc(String uniqueMethodName, List<Object> params) {
        MethodCallback methodCallback = METHOD_CALLBACK_MAP.get(uniqueMethodName);
        Objects.requireNonNull(methodCallback, "method name is not registered yet");
        return methodCallback.checkAndCallMethod(params);
    }
}
