package org.code4everything.wetool.plugin.support.exception;

import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author pantao
 * @since 2021/4/25
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class HttpException extends RuntimeException {

    protected HttpResponseStatus status;

    protected String msg;

    protected String contentType = "text/plain;charset=utf-8";
}
