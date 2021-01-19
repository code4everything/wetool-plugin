package org.code4everything.wetool.plugin.support.func;

import cn.hutool.core.lang.Pair;
import lombok.experimental.UtilityClass;

import java.util.*;
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
     * 获取所有已注册方法名称
     *
     * @return 方法名称列表
     */
    public static Set<String> getRegisteredMethodNames() {
        return Collections.unmodifiableSet(METHOD_CALLBACK_MAP.keySet());
    }

    /**
     * 获取方法详情
     *
     * @param methodName 方法名称
     *
     * @return 方法详情
     */
    public static MethodCallback getMethodDetail(String methodName) {
        return METHOD_CALLBACK_MAP.get(methodName);
    }

    /**
     * 检测一个方法是否注册
     *
     * @param methodName 方法名称
     *
     * @return 是否注册
     */
    public static boolean existsMethodName(String methodName) {
        return METHOD_CALLBACK_MAP.containsKey(methodName);
    }

    /**
     * 调用方法
     *
     * @param methodName 方法全局唯一名称
     * @param params 调用参数
     *
     * @return 执行结果
     */
    public static Object callFunc(String methodName, List<Object> params) {
        MethodCallback methodCallback = METHOD_CALLBACK_MAP.get(methodName);
        Objects.requireNonNull(methodCallback, "method name is not registered yet");
        return methodCallback.checkAndCallMethod(params);
    }

    /**
     * 如果方法存在时调用方法
     *
     * @param methodName 方法全局唯一名称
     * @param params 调用参数
     *
     * @return pair: key是一个布尔值，告诉你是否执行了方法，value是执行结果
     */
    public static Pair<Boolean, Object> callFuncIfExists(String methodName, List<Object> params) {
        MethodCallback methodCallback = METHOD_CALLBACK_MAP.get(methodName);
        if (Objects.isNull(methodCallback)) {
            return new Pair<>(false, null);
        }
        Object result = methodCallback.checkAndCallMethod(params);
        return new Pair<>(true, result);
    }
}
