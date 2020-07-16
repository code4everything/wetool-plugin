/**
 * @author pantao
 * @since 2020/1/10
 */
module wetool.plugin.everywhere {
    requires lucene.core;
    requires lucene.analyzers.smartcn;
    requires lucene.queries;
    requires lucene.queryparser;

    requires java.base;

    requires hutool.core;
    requires boot.surface;
    requires fastjson;

    requires org.code4everything.wetool.plugin.support;
}