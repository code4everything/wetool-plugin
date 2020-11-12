package org.code4everything.wetool.plugin.dbops;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import com.ql.util.express.DefaultContext;
import com.ql.util.express.ExpressRunner;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.code4everything.wetool.plugin.support.druid.JdbcExecutor;
import org.code4everything.wetool.plugin.support.util.FxDialogs;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author pantao
 * @since 2020/11/11
 */
@UtilityClass
public class ScriptExecutor {

    private static final String CLASS_NAME = ScriptExecutor.class.getName();

    @SneakyThrows
    public static void execute(String dbName, String codes, Map<String, Object> args) {
        DefaultContext<String, Object> context = new DefaultContext<>();
        if (CollUtil.isNotEmpty(args)) {
            context.putAll(args);
        }

        ExpressRunner runner = new ExpressRunner();

        runner.addFunctionOfClassMethod("dialog", CLASS_NAME, "dialog", new Class[]{Object.class}, null);
        runner.addFunctionOfClassMethod("log", Console.class, "log", new Class[]{Object.class, Object[].class}, null);
        Class<?>[] formatParamTypes = {CharSequence.class, Object[].class};
        runner.addFunctionOfClassMethod("format", StrUtil.class, "format", formatParamTypes, null);

        JdbcExecutor jdbcExecutor = JdbcExecutor.getJdbcExecutor(dbName);
        Class<?>[] sqlParamTypes = {String.class, List.class};
        runner.addFunctionOfServiceMethod("query", jdbcExecutor, "select", sqlParamTypes, null);
        runner.addFunctionOfServiceMethod("update", jdbcExecutor, "update", sqlParamTypes, null);
        runner.execute(codes, context, null, true, false);
    }

    public static void dialog(Object object) {
        if (Objects.isNull(object)) {
            return;
        }
        String header = "结果";
        if (object instanceof String) {
            FxDialogs.showInformation(header, (String) object);
        }
    }
}