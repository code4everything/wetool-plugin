package org.code4everything.wetool.plugin.support.druid;

import java.lang.annotation.*;

/**
 * @author pantao
 * @since 2020/11/10
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonField {

    String DEFAULT_METHOD_NAME = "setXxx";

    /**
     * using with listType
     */
    boolean isList() default false;

    /**
     * using with objectType
     */
    boolean isObject() default true;

    /**
     * using with methodName,
     */
    boolean isCustom() default false;

    Class<?> listType() default Object.class;

    Class<?> objectType() default None.class;

    /**
     * the method just one parameter, and type only String, like #setXxx(String)
     */
    String methodName() default DEFAULT_METHOD_NAME;
}

class None {}

