/**
 * @author pantao
 * @since 2020/1/10
 */
module wetool.plugin.qiniu {
    requires qiniu.java.sdk;
    requires com.google.common;
    requires hutool.http;

    requires java.base;

    requires hutool.core;
    requires boot.surface;
    requires fastjson;

    requires org.code4everything.wetool.plugin.support;
}