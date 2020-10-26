package org.code4everything.wetool.plugin.support.event;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.thread.ThreadUtil;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.code4everything.wetool.plugin.support.event.handler.BaseClipboardChangedEventHandler;
import org.code4everything.wetool.plugin.support.event.handler.BaseNoMessageEventHandler;
import org.code4everything.wetool.plugin.support.event.handler.BaseQuickStartClickedEventHandler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author pantao
 * @since 2020/10/25
 */
@Slf4j
@UtilityClass
public class EventCenter {

    /**
     * 注意这个事件没有 EventMessage 对象，订阅事件时可使用 {@link BaseNoMessageEventHandler}
     */
    public static final String EVENT_SECONDS_TIMER = "wetool_timer_seconds";

    /**
     * 点击快启菜单，订阅事件时可使用 {@link BaseQuickStartClickedEventHandler}
     */
    public static final String EVENT_QUICK_START_CLICKED = "wetool_quick_start_clicked";

    /**
     * 清楚所有缓存事件，订阅事件可使用 {@link BaseNoMessageEventHandler}
     */
    public static final String EVENT_CLEAR_FXML_CACHE = "wetool_clear_fxml_cache";

    /**
     * WeTool重启事件，该事件包括推出事件，订阅事件可使用 {@link BaseNoMessageEventHandler}
     */
    public static final String EVENT_WETOOL_RESTART = "wetool_restart";

    /**
     * WeTool退出事件，订阅事件可使用 {@link BaseNoMessageEventHandler}
     */
    public static final String EVENT_WETOOL_EXIT = "wetool_exit";

    /**
     * WeTool界面显示事件，订阅事件可使用 {@link BaseNoMessageEventHandler}
     */
    public static final String EVENT_WETOOL_SHOW = "wetool_show";

    /**
     * WeTool界面隐藏事件，订阅事件可使用 {@link BaseNoMessageEventHandler}
     */
    public static final String EVENT_WETOOL_HIDDEN = "wetool_hidden";

    /**
     * 剪贴板变化事件（仅针对文本内容），订阅事件可使用 {@link BaseClipboardChangedEventHandler}
     */
    public static final String EVENT_CLIPBOARD_CHANGED = "event_clipboard_changed";

    private static final Map<String, EventMode> EVENT_MAP = new ConcurrentHashMap<>();

    private static final Map<String, List<EventHandler>> HANDLER_MAP = new ConcurrentHashMap<>();

    /**
     * 注册一个事件，你在发布一个事件之前需要先注册他
     *
     * @param eventKey 事件KEY，不可重复
     * @param eventMode 事件订阅模式，单订阅或多订阅者
     *
     * @return 是否注册成功
     */
    public static Optional<EventPublisher> registerEvent(String eventKey, EventMode eventMode) {
        Objects.requireNonNull(eventKey);
        Objects.requireNonNull(eventMode);

        if (EVENT_MAP.containsKey(eventKey)) {
            log.warn("event '{}' is already registered!", eventKey);
            return Optional.empty();
        }

        EVENT_MAP.put(eventKey, eventMode);
        log.info("event '{}' register success!", eventKey);
        return Optional.of(new EventPublisher(eventKey));
    }

    /**
     * 发布一个事件，发布事件之前你需要先注册事件
     *
     * @param eventKey 事件KEY
     * @param eventTime 事件发生的时间
     *
     * @return 事件是否发布成功
     */
    public static boolean publishEvent(String eventKey, Date eventTime) {
        return publishEvent(eventKey, eventTime, null);
    }

    /**
     * 发布一个事件，发布事件之前你需要先注册事件
     *
     * @param eventKey 事件KEY
     * @param eventTime 事件发生的时间
     * @param eventMessage 自定义事件消息，可不传
     *
     * @return 事件是否发布成功
     */
    public static boolean publishEvent(String eventKey, Date eventTime, EventMessage eventMessage) {
        Objects.requireNonNull(eventKey);
        Objects.requireNonNull(eventTime);

        if (!EVENT_MAP.containsKey(eventKey)) {
            log.debug("please register event '{}' first!", eventKey);
            return false;
        }

        List<EventHandler> list = HANDLER_MAP.get(eventKey);
        if (CollUtil.isEmpty(list)) {
            log.debug("event '{}' no any subscriber!", eventKey);
            return true;
        }

        ThreadUtil.execute(() -> {
            if (list.size() == 1) {
                list.get(0).handleEvent(eventKey, eventTime, eventMessage);
            } else {
                list.forEach(e -> ThreadUtil.execute(() -> e.handleEvent(eventKey, eventTime, eventMessage)));
            }
        });

        String time = DateUtil.format(eventTime, DatePattern.NORM_DATETIME_MS_FORMAT);
        log.debug("publish event '{}' success, event time: {}", eventKey, time);
        return true;
    }

    /**
     * 订阅事件
     *
     * @param eventKey 事件KEY
     * @param eventHandler 事件处理器
     *
     * @return 是否订阅成功
     */
    public static boolean subscribeEvent(String eventKey, EventHandler eventHandler) {
        Objects.requireNonNull(eventKey);
        Objects.requireNonNull(eventHandler);

        EventMode eventMode = EVENT_MAP.get(eventKey);
        if (Objects.isNull(eventMode)) {
            log.debug("event '{}' is not register yet!", eventKey);
        }

        if (eventMode == EventMode.SINGLE_SUB) {
            List<EventHandler> list = HANDLER_MAP.get(eventKey);
            if (CollUtil.isNotEmpty(list)) {
                log.warn("event '{}' only allow single subscriber!", eventKey);
                return false;
            }
        }

        HANDLER_MAP.computeIfAbsent(eventKey, s -> new ArrayList<>()).add(eventHandler);
        return true;
    }
}
