package org.code4everything.wetool.plugin.support.factory;

import cn.hutool.core.util.ObjectUtil;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.code4everything.boot.base.ReferenceUtils;
import org.code4everything.wetool.plugin.support.BaseViewController;
import org.code4everything.wetool.plugin.support.constant.AppConsts;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 保存打开的窗口控制器对象
 *
 * @author pantao
 * @since 2018/3/31
 */
@Slf4j
@UtilityClass
public class BeanFactory {

    private static final Map<Class<?>, Object> SINGLETON_MAPPING = new ConcurrentHashMap<>(16);

    private static final Map<String, WeakReference<BaseViewController>> VIEW_MAPPING = new ConcurrentHashMap<>(16);

    private static final Map<String, SoftReference<Object>> PROTOTYPE_MAPPING = new ConcurrentHashMap<>(16);

    /**
     * 注册多例Bean
     */
    public static void register(String name, Object bean) {
        if (isRegistered(name)) {
            // 已注册
            String msg = "key[{}] is already registered, registered class is {}, register failed class: {}";
            log.warn(msg, name, get(name).getClass().getName(), bean.getClass().getName());
            return;
        }
        PROTOTYPE_MAPPING.put(name, new SoftReference<>(bean));
    }

    /**
     * 获取多例的Bean
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(String name) {
        return (T) ReferenceUtils.unwrap(PROTOTYPE_MAPPING.get(name));
    }

    /**
     * 注册单例Bean
     */
    public static <T> void register(T bean) {
        if (isRegistered(bean.getClass())) {
            // 已注册
            log.warn("class[{}] is already registered", bean.getClass().getName());
            return;
        }
        SINGLETON_MAPPING.put(bean.getClass(), bean);
    }

    /**
     * 注册视图Bean，插件请调用下面的 {@link #registerView(String, String, BaseViewController)}方法
     */
    public static void registerView(String tabName, BaseViewController viewController) {
        registerView(AppConsts.Title.APP_TITLE, tabName, viewController);
    }

    /**
     * 注册视图Bean，插件注册视图请调用此方法，以免冲突
     */
    public static void registerView(String tabId, String tabName, BaseViewController viewController) {
        String viewName = tabId + tabName;
        if (isViewRegistered(viewName)) {
            // 已注册
            String msg = "key[{}] is already registered, registered view class is {}, register failed class: {}";
            log.warn(msg, viewName, getView(viewName).getClass().getName(), viewController.getClass().getName());
            return;
        }
        VIEW_MAPPING.put(tabId + tabName, new WeakReference<>(viewController));
    }

    /**
     * 获取单例Bean
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> clazz) {
        return (T) SINGLETON_MAPPING.get(clazz);
    }

    /**
     * 获取视图Bean
     *
     * @param viewName 由tabId和tabName拼接而成
     */
    public static BaseViewController getView(String viewName) {
        return ReferenceUtils.unwrap(VIEW_MAPPING.get(viewName));
    }

    /**
     * 获取视图Bean
     *
     * @param viewName 由tabId和tabName拼接而成
     */
    @SuppressWarnings("unchecked")
    public static <T extends BaseViewController> T getViewObject(String viewName) {
        return (T) getView(viewName);
    }

    /**
     * 单例Bean是否注册
     */
    public static boolean isRegistered(Class<?> clazz) {
        return SINGLETON_MAPPING.containsKey(clazz);
    }

    /**
     * 视图Bean是否注册
     *
     * @param viewName 由tabId和tabName拼接而成
     */
    public static boolean isViewRegistered(String viewName) {
        return VIEW_MAPPING.containsKey(viewName) && ObjectUtil.isNotNull(ReferenceUtils.unwrap(VIEW_MAPPING.get(viewName)));
    }

    /**
     * 多例Bean是否注册
     *
     * @param name Bean Name
     */
    public static boolean isRegistered(String name) {
        return PROTOTYPE_MAPPING.containsKey(name) && ObjectUtil.isNotNull(ReferenceUtils.unwrap(PROTOTYPE_MAPPING.get(name)));
    }
}
