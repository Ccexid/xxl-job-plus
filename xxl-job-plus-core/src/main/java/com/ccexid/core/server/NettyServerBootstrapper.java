package com.ccexid.core.server;

import com.ccexid.core.biz.ExecutorBiz;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

/**
 * Netty服务器引导类
 * 负责Netty服务器的配置与启动
 */
public class NettyServerBootstrapper {
    private static final int IDLE_TIMEOUT_SECONDS = 90;
    private static final int HTTP_AGGREGATOR_MAX_SIZE = 5 * 1024 * 1024;

    private final ExecutorBiz executorBiz;
    private final ThreadPoolExecutor bizThreadPool;
    private BiFunction<ExecutorBiz, String, EmbeddedHttpServerHandler> handlerFactory;

    public NettyServerBootstrapper(ExecutorBiz executorBiz, ThreadPoolExecutor bizThreadPool) {
        this.executorBiz = executorBiz;
        this.bizThreadPool = bizThreadPool;
        // 默认处理器工厂
        this.handlerFactory = (biz, accessToken) -> new EmbeddedHttpServerHandler(biz, accessToken, bizThreadPool);
    }

    /**
     * 设置自定义处理器工厂
     * @param handlerFactory 处理器工厂函数
     */
    public void setHandlerFactory(BiFunction<ExecutorBiz, String, EmbeddedHttpServerHandler> handlerFactory) {
        this.handlerFactory = handlerFactory;
    }

    /**
     * 启动Netty服务器
     */
    public ChannelFuture bootstrap(EventLoopGroup bossGroup, EventLoopGroup workerGroup,
                                   int port, String accessToken) throws InterruptedException {
        ServerBootstrap bootstrap = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(createChannelInitializer(accessToken))
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        return bootstrap.bind(port).sync();
    }

    /**
     * 创建Channel初始化器
     */
    private ChannelInitializer<SocketChannel> createChannelInitializer(String accessToken) {
        return new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel channel) {
                channel.pipeline()
                        .addLast(new IdleStateHandler(0, 0, IDLE_TIMEOUT_SECONDS, TimeUnit.SECONDS))
                        .addLast(new HttpServerCodec())
                        .addLast(new HttpObjectAggregator(HTTP_AGGREGATOR_MAX_SIZE))
                        .addLast(handlerFactory.apply(executorBiz, accessToken));
            }
        };
    }

    /**
     * 获取业务线程池
     * @return 业务线程池
     */
    public ThreadPoolExecutor getBizThreadPool() {
        return bizThreadPool;
    }

    /**
     * 获取执行器
     * @return 执行器
     */
    public ExecutorBiz getExecutorBiz() {
        return executorBiz;
    }
}