package org.code4everything.wetool.plugin.support.exception;

import java.util.Objects;

/**
 * @author pantao
 * @since 2020/12/6
 */
public class HttpBadReqException extends RuntimeException {

    public HttpBadReqException(String msg) {
        super(msg);
        Objects.requireNonNull(msg);
    }
}
