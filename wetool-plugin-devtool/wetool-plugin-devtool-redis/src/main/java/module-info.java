/**
 * @author pantao
 * @since 2020/1/10
 */
module wetool.plugin.devtool.redis {
    requires jedis;

    requires java.base;

    requires hutool.core;
    requires boot.surface;
    requires fastjson;

    requires org.code4everything.wetool.plugin.support;
}