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

/**
 * Netty服务器引导类
 * 负责Netty服务器的配置与启动
 */
public class NettyServerBootstrapper {
    private static final int IDLE_TIMEOUT_SECONDS = 90;
    private static final int HTTP_AGGREGATOR_MAX_SIZE = 5 * 1024 * 1024;

    private final ExecutorBiz executorBiz;
    private final ThreadPoolExecutor bizThreadPool;

    public NettyServerBootstrapper(ExecutorBiz executorBiz, ThreadPoolExecutor bizThreadPool) {
        this.executorBiz = executorBiz;
        this.bizThreadPool = bizThreadPool;
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
                        .addLast(new EmbeddedHttpServerHandler(executorBiz, accessToken, bizThreadPool));
            }
        };
    }
}
