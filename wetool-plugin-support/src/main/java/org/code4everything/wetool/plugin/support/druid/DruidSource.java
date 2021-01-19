package org.code4everything.wetool.plugin.support.druid;

import com.alibaba.druid.pool.DruidDataSource;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.code4everything.wetool.plugin.support.util.FxDialogs;

import java.sql.SQLException;
import java.util.*;

/**
 * @author pantao
 * @since 2020/11/10
 */
@Slf4j
@UtilityClass
public class DruidSource {

    private static final Map<String, DruidDataSource> MAP = new LinkedHashMap<>();

    public static void configDataSource(Properties properties) {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.configFromPropety(properties);
        try {
            dataSource.init();
        } catch (SQLException e) {
            FxDialogs.showException("数据库连接失败", e);
            return;
        }
        log.info("db connected: {}", properties);
        MAP.put(dataSource.getName(), dataSource);
    }

    public static DruidDataSource getDruidDataSource(String name) {
        return MAP.get(name);
    }

    public static Set<String> listAllNames() {
        return Collections.unmodifiableSet(MAP.keySet());
    }

    public static Collection<DruidDataSource> listAllDataSources() {
        return Collections.unmodifiableCollection(MAP.values());
    }
}
