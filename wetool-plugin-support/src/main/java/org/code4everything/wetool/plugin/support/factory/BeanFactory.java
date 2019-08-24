package org.code4everything.wetool.plugin.support.factory;

import cn.hutool.core.util.StrUtil;
import lombok.experimental.UtilityClass;
import org.code4everything.wetool.plugin.support.BaseViewController;
import org.code4everything.wetool.plugin.support.constant.AppConsts;
import org.code4everything.wetool.plugin.support.exception.BeanException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 保存打开的窗口控制器对象
 *
 * @author pantao
 * @since 2018/3/31
 */
@UtilityClass
public class BeanFactory {

    private static final Map<Class<?>, Object> CLASS_MAPPING = new ConcurrentHashMap<>(16);

    private static final Map<String, BaseViewController> TITLE_MAPPING = new ConcurrentHashMap<>(16);

    private static final Map<String, Object> PROTOTYPE_MAPPING = new ConcurrentHashMap<>(16);

    /**
     * 注册多例Bean
     */
    public static void register(String name, Object bean) {
        PROTOTYPE_MAPPING.put(name, bean);
    }

    /**
     * 获取多例的Bean
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(String name) {
        return (T) PROTOTYPE_MAPPING.get(name);
    }

    /**
     * 注册单例Bean
     */
    public static <T> void register(T bean) {
        CLASS_MAPPING.put(bean.getClass(), bean);
    }

    /**
     * 注册视图Bean
     */
    public static void registerView(String tabName, BaseViewController viewController) {
        registerView(AppConsts.Title.APP_TITLE, tabName, viewController);
    }

    /**
     * 注册视图Bean，插件注册视图请调用此方法，以免冲突
     */
    public static void registerView(String tabId, String tabName, BaseViewController viewController) {
        register(viewController);
        TITLE_MAPPING.put(tabId + tabName, viewController);
    }

    /**
     * 获取单例Bean
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> clazz) {
        return (T) CLASS_MAPPING.get(clazz);
    }

    /**
     * 获取视图Bean
     *
     * @param viewName 由tabId和tabName拼接而成
     */
    public static BaseViewController getView(String viewName) {
        return TITLE_MAPPING.get(viewName);
    }

    public static boolean isRegistered(Class<?> clazz) {
        return CLASS_MAPPING.containsKey(clazz);
    }

    public static <T> T safelyGet(Class<T> clazz) {
        if (isRegistered(clazz)) {
            return get(clazz);
        }
        throw new BeanException(StrUtil.format("bean '{}' has not registered", clazz.getName()));
    }
}
