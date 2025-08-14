package com.ccexid.core.util;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * net util
 *
 * @author xuxueli 2017-11-29 17:00:25
 */
@Slf4j
public class NetUtil {

    /**
     * find available port
     *
     * @param defaultPort default port
     * @return available port
     */
    public static int findAvailablePort(int defaultPort) {
        int portTemp = defaultPort;
        while (portTemp < 65535) {
            if (!isPortUsed(portTemp)) {
                return portTemp;
            } else {
                portTemp++;
            }
        }
        portTemp = defaultPort - 1;
        while (portTemp > 0) {
            if (!isPortUsed(portTemp)) {
                return portTemp;
            } else {
                portTemp--;
            }
        }
        throw new RuntimeException("no available port.");
    }

    /**
     * check port used
     *
     * @param port port
     * @return true if port is used
     */
    public static boolean isPortUsed(int port) {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            return false;
        } catch (IOException e) {
            log.info(">>>>>>>>>>> xxl-job, port[{}] is in use.", port);
            return true;
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    log.error("serverSocket close failed.", e);
                }
            }
        }
    }

}
