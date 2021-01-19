package org.code4everything.wetool.plugin.devtool.ssh.ssh;

import lombok.experimental.UtilityClass;
import org.code4everything.boot.base.ReferenceUtils;
import org.code4everything.wetool.plugin.devtool.ssh.config.ServerConfiguration;
import org.code4everything.wetool.plugin.devtool.ssh.hutool.Sftp;

import java.lang.ref.WeakReference;
import java.util.*;

/**
 * @author pantao
 * @since 2019/11/25
 */
@UtilityClass
public class SftpUtils {

    private static final Map<String, ServerConfiguration> CONFIGURATION_MAP = new HashMap<>();

    private static final Map<String, WeakReference<Sftp>> SFTP_MAP = new HashMap<>();

    public static ServerConfiguration getConf(String alias) {
        return CONFIGURATION_MAP.get(alias);
    }

    public static Collection<ServerConfiguration> listConf() {
        return Collections.unmodifiableCollection(CONFIGURATION_MAP.values());
    }

    public static void putConf(ServerConfiguration server) {
        CONFIGURATION_MAP.put(server.getAlias(), server);
    }

    public static void clear() {
        CONFIGURATION_MAP.clear();
        SFTP_MAP.clear();
    }

    public static Sftp getSftp(String alias) {
        Sftp sftp = ReferenceUtils.unwrap(SFTP_MAP.get(alias));
        if (Objects.isNull(sftp)) {
            ServerConfiguration conf = CONFIGURATION_MAP.get(alias);
            sftp = new Sftp(conf.getHost(), conf.getPort(), conf.getUsername(), conf.getPassword(), conf.getCharset());
            SFTP_MAP.put(alias, new WeakReference<>(sftp));
        }
        return sftp.reconnectIfTimeout();
    }
}
