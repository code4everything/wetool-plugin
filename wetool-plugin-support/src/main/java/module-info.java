/**
 * @author pantao
 * @since 2020/1/9
 */
module org.code4everything.wetool.plugin.support {
    requires transitive lombok;

    // Java依赖
    requires java.base;
    requires transitive java.sql;
    requires transitive java.xml;
    requires transitive java.naming;
    requires transitive java.logging;
    requires transitive java.desktop;
    requires transitive java.net.http;
    requires transitive java.compiler;
    requires transitive java.scripting;
    requires transitive java.instrument;
    requires transitive java.management;
    requires transitive java.transaction.xa;

    // JDK依赖
    requires transitive jdk.crypto.ec;
    requires transitive jdk.unsupported;

    // JavaFX依赖
    requires transitive javafx.fxml;
    requires transitive javafx.controls;
    requires transitive javafx.graphics;
    requires transitive javafx.web;
    requires transitive javafx.media;
    requires transitive javafx.swing;

    // 工具包依赖
    requires transitive org.slf4j;

    requires hutool.core;
    requires boot.surface;
    requires fastjson;
    requires hutool.system;
    requires com.google.common;
    requires druid;
    requires mysql.connector.java;
    requires jnativehook;
    requires io.netty.common;
    requires io.netty.buffer;
    requires io.netty.codec;
    requires io.netty.codec.http;
    requires io.netty.transport;
    requires io.netty.handler;
    requires hutool.cache;
    requires io.vavr;
    requires smartgraph;

    // 导出包
    exports org.code4everything.wetool.plugin.support;
    exports org.code4everything.wetool.plugin.support.config;
    exports org.code4everything.wetool.plugin.support.constant;
    exports org.code4everything.wetool.plugin.support.control;
    exports org.code4everything.wetool.plugin.support.control.cell;
    exports org.code4everything.wetool.plugin.support.exception;
    exports org.code4everything.wetool.plugin.support.factory;
    exports org.code4everything.wetool.plugin.support.util;
    exports org.code4everything.wetool.plugin.support.event;
    exports org.code4everything.wetool.plugin.support.event.message;
    exports org.code4everything.wetool.plugin.support.event.handler;
    exports org.code4everything.wetool.plugin.support.druid;
    exports org.code4everything.wetool.plugin.support.listener;
    exports org.code4everything.wetool.plugin.support.http;

    // 开放反射权限
    opens org.code4everything.wetool.plugin.support.config;
}
