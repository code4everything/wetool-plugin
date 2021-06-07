package org.code4everything.wetool.plugin.support.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author pantao
 * @since 2021/6/7
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class WeStatus {

    private State state;

    public enum State {

        /**
         * 启动中
         */
        STARTING,

        /**
         * 运行中
         */
        RUNNING,

        /**
         * 停止中
         */
        TERMINATING
    }
}
