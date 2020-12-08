package org.code4everything.wetool.plugin.support.http;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.experimental.UtilityClass;
import org.code4everything.wetool.plugin.support.exception.HttpBadReqException;

import java.util.Objects;

/**
 * @author pantao
 * @since 2020/12/8
 */
@UtilityClass
public class ArgRequires {

    public static void url(JSONObject args, String name) {
        Objects.requireNonNull(args);
        String errMsg = "field[{}] must be a url";
        match(Validator.isUrl(args.getString(name)), errMsg, name);
    }

    public static void mobile(JSONObject args, String name) {
        Objects.requireNonNull(args);
        String errMsg = "field[{}] must be a chinese mobile phone number";
        match(Validator.isMobile(args.getString(name)), errMsg, name);
    }

    public static void email(JSONObject args, String name) {
        Objects.requireNonNull(args);
        String errMsg = "field[{}] must be a email address";
        match(Validator.isEmail(args.getString(name)), errMsg, name);
    }

    public static void number(JSONObject args, String name) {
        Objects.requireNonNull(args);
        String errMsg = "field[{}] must be a number";
        match(Validator.isNumber(args.getString(name)), errMsg, name);
    }

    public static void regex(JSONObject args, String name, String regex) {
        Objects.requireNonNull(args);
        String errMsg = "field[{}] required regex pattern: {}";
        match(Validator.isMatchRegex(regex, args.getString(name)), errMsg, name, regex);
    }

    public static void notEmpty(JSONObject args, String name) {
        Objects.requireNonNull(args);
        String errMsg = "field[{}] must not be empty";
        match(Validator.isNotEmpty(args.getString(name)), errMsg, name);
    }

    public static void notNull(JSONObject args, String name) {
        Objects.requireNonNull(args);
        String errMsg = "missing required arguments: {}";
        match(Validator.isNotNull(args.get(name)), errMsg, name);
    }

    public static void match(boolean match, String errMsg, Object... params) {
        if (match) {
            return;
        }
        throw new HttpBadReqException(StrUtil.format(errMsg, params));
    }
}
