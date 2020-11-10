package org.code4everything.wetool.plugin.support.druid;

import cn.hutool.core.lang.Holder;

import java.sql.Wrapper;

/**
 * @author pantao
 * @since 2020/11/10
 */
@FunctionalInterface
interface ExecutorCaller<T extends Wrapper> {

    void executeAndSetSql(T sqlExecutor, Holder<String> sqlWrapper) throws Exception;
}

