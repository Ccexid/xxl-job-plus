package com.ccexid.core.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 业务线程池工厂
 * 负责创建和管理业务处理线程池
 */
public class BizThreadPoolFactory {
    private static final Logger logger = LoggerFactory.getLogger(BizThreadPoolFactory.class);
    private static final int MAX_SIZE = 200;
    private static final int QUEUE_SIZE = 2000;
    private static final int KEEP_ALIVE_SECONDS = 60;

    /**
     * 创建业务线程池
     */
    public static ThreadPoolExecutor create() {
        return new ThreadPoolExecutor(
                0,
                MAX_SIZE,
                KEEP_ALIVE_SECONDS,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(QUEUE_SIZE),
                BizThreadPoolFactory::createBizThread,
                BizThreadPoolFactory::rejectBizTask
        );
    }

    /**
     * 创建业务线程池（可自定义参数）
     * @param coreSize 核心线程数
     * @param maxSize 最大线程数
     * @param queueSize 队列大小
     * @param keepAliveSeconds 空闲线程存活时间（秒）
     * @return 线程池实例
     */
    public static ThreadPoolExecutor create(int coreSize, int maxSize, int queueSize, int keepAliveSeconds) {
        return new ThreadPoolExecutor(
                coreSize,
                maxSize,
                keepAliveSeconds,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queueSize),
                BizThreadPoolFactory::createBizThread,
                BizThreadPoolFactory::rejectBizTask
        );
    }

    /**
     * 创建业务线程
     */
    private static Thread createBizThread(Runnable r) {
        Thread thread = new Thread(r, "xxl-job-embed-server-biz-" + r.hashCode());
        thread.setDaemon(true);
        return thread;
    }

    /**
     * 线程池拒绝策略
     */
    private static void rejectBizTask(Runnable r, ThreadPoolExecutor executor) {
        throw new RuntimeException("XXL-Job业务线程池已耗尽!");
    }

    /**
     * 关闭线程池
     */
    public static void shutdown(ThreadPoolExecutor threadPool) {
        if (threadPool != null && !threadPool.isShutdown()) {
            try {
                // 等待任务执行完成，最多等待60秒
                if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                    // 强制关闭
                    threadPool.shutdownNow();
                    // 再次等待关闭完成
                    if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                        logger.warn("业务线程池未能正常关闭");
                    }
                }
            } catch (InterruptedException e) {
                threadPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}