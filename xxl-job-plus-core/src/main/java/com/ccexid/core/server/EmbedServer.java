package com.ccexid.core.server;

import com.ccexid.core.service.ExecutorService;
import com.ccexid.core.service.impl.ExecutorServiceImpl;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 嵌入式服务器主类
 * 协调各组件启动与停止，管理服务器生命周期
 */
public class EmbedServer {
    private static final Logger logger = LoggerFactory.getLogger(EmbedServer.class);

    private final ThreadPoolExecutor bizThreadPool;
    private final NettyServerBootstrapper nettyBootstrapper;
    private final RegistrationManager registrationManager;
    private Thread serverThread;

    public EmbedServer() {
        ExecutorService executorService = new ExecutorServiceImpl();
        this.bizThreadPool = BizThreadPoolFactory.create();
        this.nettyBootstrapper = new NettyServerBootstrapper(executorService, bizThreadPool);
        this.registrationManager = new RegistrationManager();
    }

    /**
     * 启动服务器
     */
    public void start(final String address, final int port, final String appName, final String accessToken) {
        serverThread = new Thread(() -> startServer(address, port, appName, accessToken),
                "xxl-job-embed-server-main-thread");
        serverThread.setDaemon(true);
        serverThread.start();
    }

    private void startServer(String address, int port, String appName, String accessToken) {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ChannelFuture future = nettyBootstrapper.bootstrap(bossGroup, workerGroup, port, accessToken);
            registrationManager.startRegistration(appName, address);
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            logger.info(">>>>>>>>>>> XXL-Job嵌入式服务器已停止");
        } catch (Throwable e) {
            logger.error(">>>>>>>>>>> XXL-Job嵌入式服务器启动失败", e);
        } finally {
            NettyResourceManager.shutdown(bossGroup, workerGroup);
            BizThreadPoolFactory.shutdown(bizThreadPool);
        }
    }

    /**
     * 停止服务器
     */
    public void stop() {
        if (serverThread != null && serverThread.isAlive()) {
            serverThread.interrupt();
        }
        registrationManager.stopRegistration();
        logger.info(">>>>>>>>>>> XXL-Job嵌入式服务器已销毁");
    }
}
