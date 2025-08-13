package com.ccexid.core.server;

import com.ccexid.core.biz.ExecutorBiz;
import com.ccexid.core.biz.impl.ExecutorBizImpl;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final ExecutorBiz executorBiz;

    public EmbedServer() {
        this.executorBiz = new ExecutorBizImpl();
        this.bizThreadPool = BizThreadPoolFactory.create();
        this.nettyBootstrapper = new NettyServerBootstrapper(executorBiz, bizThreadPool);
        this.registrationManager = new RegistrationManager();
    }

    /**
     * 构造函数，允许自定义ExecutorBiz实现
     *
     * @param executorBiz ExecutorBiz实现
     */
    public EmbedServer(ExecutorBiz executorBiz) {
        this.executorBiz = executorBiz;
        this.bizThreadPool = BizThreadPoolFactory.create();
        this.nettyBootstrapper = new NettyServerBootstrapper(executorBiz, bizThreadPool);
        this.registrationManager = new RegistrationManager();
    }

    /**
     * 构造函数，允许自定义ExecutorBiz实现和线程池参数
     *
     * @param executorBiz      ExecutorBiz实现
     * @param corePoolSize     核心线程数
     * @param maxPoolSize      最大线程数
     * @param queueSize        队列大小
     * @param keepAliveSeconds 空闲线程存活时间（秒）
     */
    public EmbedServer(ExecutorBiz executorBiz, int corePoolSize, int maxPoolSize, int queueSize, int keepAliveSeconds) {
        this.executorBiz = executorBiz;
        this.bizThreadPool = BizThreadPoolFactory.create(corePoolSize, maxPoolSize, queueSize, keepAliveSeconds);
        this.nettyBootstrapper = new NettyServerBootstrapper(executorBiz, bizThreadPool);
        this.registrationManager = new RegistrationManager();
    }

    /**
     * 启动服务器
     */
    public void start(final String address, final int port, final String appName, final String accessToken) {
        if (!isRunning.compareAndSet(false, true)) {
            logger.warn("服务器已在运行中");
            return;
        }

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
            logger.info(">>>>>>>>>>> XXL-Job嵌入式服务器启动成功，端口:{}", port);
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            if (isRunning.get()) {
                logger.info(">>>>>>>>>>> XXL-Job嵌入式服务器已停止");
            }
        } catch (Throwable e) {
            logger.error(">>>>>>>>>>> XXL-Job嵌入式服务器启动失败", e);
        } finally {
            NettyResourceManager.shutdownWithTimeout(bossGroup, workerGroup);
            BizThreadPoolFactory.shutdown(bizThreadPool);
            isRunning.set(false);
        }
    }

    /**
     * 停止服务器
     */
    public void stop() {
        if (isRunning.compareAndSet(true, false)) {
            if (serverThread != null && serverThread.isAlive()) {
                serverThread.interrupt();
            }
            registrationManager.stopRegistration();
            logger.info(">>>>>>>>>>> XXL-Job嵌入式服务器停止指令已发送");
        }
    }

    /**
     * 检查服务器是否正在运行
     *
     * @return 是否正在运行
     */
    public boolean isRunning() {
        return isRunning.get();
    }

    /**
     * 获取业务线程池
     *
     * @return 业务线程池
     */
    public ThreadPoolExecutor getBizThreadPool() {
        return bizThreadPool;
    }

    /**
     * 获取Netty服务器引导器
     *
     * @return Netty服务器引导器
     */
    public NettyServerBootstrapper getNettyBootstrapper() {
        return nettyBootstrapper;
    }
}