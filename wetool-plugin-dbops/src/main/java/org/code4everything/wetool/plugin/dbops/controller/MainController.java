package org.code4everything.wetool.plugin.dbops.controller;

import cn.hutool.core.lang.Console;
import com.alibaba.fastjson.JSON;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import org.code4everything.wetool.plugin.support.BaseViewController;
import org.code4everything.wetool.plugin.support.druid.DruidSource;
import org.code4everything.wetool.plugin.support.druid.JdbcExecutor;
import org.code4everything.wetool.plugin.support.factory.BeanFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author pantao
 * @since 2020/11/11
 */
public class MainController implements BaseViewController {

    public static final String TAB_ID = "ease-db-ops";

    public static final String TAB_NAME = "数据库小应用";

    @FXML
    public TextArea sqlText;

    @FXML
    private void initialize() {
        BeanFactory.registerView(TAB_ID, TAB_NAME, this);
    }

    public void execute() {
        ArrayList<String> list = new ArrayList<>(DruidSource.listAllNames());
        JdbcExecutor jdbcExecutor = JdbcExecutor.getJdbcExecutor(list.get(0));
        List<Map<String, Object>> mapList = jdbcExecutor.select(sqlText.getText(), null);
        Console.log(JSON.toJSONString(mapList, true));
    }
}
