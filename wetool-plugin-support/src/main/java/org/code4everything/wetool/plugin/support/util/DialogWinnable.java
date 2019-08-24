package org.code4everything.wetool.plugin.support.util;

/**
 * @author pantao
 * @since 2019/8/24
 */
public interface DialogWinnable<R> {

    /**
     * 转换结果
     *
     * @return 转换后的结果
     */
    default R convertResult() {return null;}

    /**
     * 获取结果
     *
     * @param result 结果
     */
    default void consumeResult(R result) {}
}
