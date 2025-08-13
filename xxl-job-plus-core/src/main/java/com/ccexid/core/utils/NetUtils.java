package com.ccexid.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * 网络工具类
 * 提供端口可用性检查、查找可用端口等网络相关操作
 *
 * @author xuxueli 2017-11-29 17:00:25
 */
public class NetUtils {
    private static final Logger logger = LoggerFactory.getLogger(NetUtils.class);
    private static final int MAX_PORT = 65535; // 最大端口号
    private static final int MIN_PORT = 1;     // 最小端口号

    /**
     * 查找可用端口
     * 从默认端口开始向上查找，若未找到则向下查找
     *
     * @param defaultPort 默认端口
     * @return 可用端口号
     * @throws RuntimeException 无可用端口时抛出
     */
    public static int findAvailablePort(int defaultPort) {
        // 从默认端口向上查找
        int port = defaultPort;
        while (port <= MAX_PORT) {
            if (!isPortInUse(port)) {
                return port;
            }
            port++;
        }

        // 向上未找到则向下查找
        port = defaultPort - 1;
        while (port >= MIN_PORT) {
            if (!isPortInUse(port)) {
                return port;
            }
            port--;
        }

        throw new RuntimeException("未找到可用端口");
    }

    /**
     * 检查端口是否已被使用
     *
     * @param port 端口号
     * @return 端口是否被使用
     */
    public static boolean isPortInUse(int port) {
        // 验证端口范围
        if (port < MIN_PORT || port > MAX_PORT) {
            throw new IllegalArgumentException("无效的端口号: " + port);
        }

        // 使用try-with-resources自动关闭ServerSocket
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            // 端口可用
            return false;
        } catch (IOException e) {
            logger.info(">>>>>>>>>>> xxl-job, 端口[{}]已被占用", port);
            // 端口不可用
            return true;
        }
    }
}
