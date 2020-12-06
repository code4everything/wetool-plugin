package org.code4everything.wetool.plugin.support.http;

import com.alibaba.fastjson.JSONArray;

import java.util.Collection;

/**
 * @author pantao
 * @since 2020/12/6
 */
public class ListResp extends JSONArray {

    public static ListResp of() {
        return new ListResp();
    }

    public static ListResp of(Object e) {
        return of().addOne(e);
    }

    public ListResp addOne(Object e) {
        super.add(e);
        return this;
    }

    public ListResp addList(Collection<?> c) {
        super.addAll(c);
        return this;
    }
}
