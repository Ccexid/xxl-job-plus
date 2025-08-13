package com.ccexid.core.server;

import io.netty.channel.EventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Netty资源管理器
 * 负责Netty线程组的关闭与资源释放
 */
public class NettyResourceManager {
    private static final Logger logger = LoggerFactory.getLogger(NettyResourceManager.class);

    /**
     * 优雅关闭EventLoopGroup
     */
    public static void shutdown(EventLoopGroup... groups) {
        for (EventLoopGroup group : groups) {
            try {
                if (group != null) {
                    group.shutdownGracefully().sync();
                }
            } catch (InterruptedException e) {
                logger.error("关闭Netty线程组失败", e);
            }
        }
    }
}