package org.code4everything.wetool.plugin.support.exception;

import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.Objects;

/**
 * @author pantao
 * @since 2020/12/6
 */
public class HttpBadReqException extends HttpException {

    public HttpBadReqException(String msg) {
        Objects.requireNonNull(msg);
        setStatus(HttpResponseStatus.BAD_REQUEST).setMsg(msg);
    }
}
