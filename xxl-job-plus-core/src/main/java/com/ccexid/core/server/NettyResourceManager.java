package com.ccexid.core.server;

import io.netty.channel.EventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Netty资源管理器
 * 负责Netty线程组的关闭与资源释放
 */
public class NettyResourceManager {
    private static final Logger logger = LoggerFactory.getLogger(NettyResourceManager.class);
    private static final int SHUTDOWN_TIMEOUT_SECONDS = 30;

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
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 优雅关闭EventLoopGroup（带超时时间）
     * @param timeoutSeconds 超时时间（秒）
     * @param groups EventLoopGroup数组
     */
    public static void shutdown(int timeoutSeconds, EventLoopGroup... groups) {
        for (EventLoopGroup group : groups) {
            try {
                if (group != null) {
                    group.shutdownGracefully(0, timeoutSeconds, TimeUnit.SECONDS).sync();
                }
            } catch (InterruptedException e) {
                logger.error("关闭Netty线程组失败", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 优雅关闭EventLoopGroup（默认超时时间）
     */
    public static void shutdownWithTimeout(EventLoopGroup... groups) {
        shutdown(SHUTDOWN_TIMEOUT_SECONDS, groups);
    }
}