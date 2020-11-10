package org.code4everything.wetool.plugin.support.druid;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author pantao
 * @since 2020/11/10
 */
@Getter
@RequiredArgsConstructor
public class SqlException extends RuntimeException {

    private final String msg;
}
