package org.code4everything.wetool.plugin.support.exception;

/**
 * @author pantao
 * @since 2019/8/25
 */
public class PluginException extends RuntimeException {

    public PluginException(String msg) {
        super(msg);
    }

    public PluginException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public PluginException(Throwable cause) {
        super(cause);
    }
}
