package org.code4everything.wetool.plugin.ftp.server.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.ConcurrentLoginPermission;
import org.apache.ftpserver.usermanager.impl.TransferRatePermission;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import org.code4everything.boot.base.bean.BaseBean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author pantao
 * @since 2019/9/23
 */
public class FtpServerUser extends BaseUser implements BaseBean, Serializable {

    private static final long serialVersionUID = 8342136773455728747L;

    /**
     * 权限，如：rw,r,w
     */
    private String auth;

    private transient List<Authority> authorities;

    @Override
    public String getHomeDirectory() {
        return StrUtil.emptyToDefault(super.getHomeDirectory(), FileUtil.getUserHomePath());
    }

    @Override
    public void setHomeDirectory(String home) {
        super.setHomeDirectory(StrUtil.emptyToDefault(home, FileUtil.getUserHomePath()));
    }

    @Override
    public List<Authority> getAuthorities() {
        if (CollUtil.isEmpty(authorities)) {
            authorities = new ArrayList<>();
            authorities.add(new ConcurrentLoginPermission(8, 2));

            // 读权限
            final String read = "r";
            if (auth.contains(read)) {
                int max = Integer.MAX_VALUE;
                authorities.add(new TransferRatePermission(max, max));
            }

            // 写权限
            final String write = "w";
            if (auth.contains(write)) {
                authorities.add(new WritePermission());
            }
        }
        return authorities;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }
}
