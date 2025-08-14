package com.ccexid.core.thread;

/**
 * 线程接口类
 * 定义了线程的基本操作接口，包括启动和停止线程的功能
 */
public interface IThread {
    /**
     * 停止线程
     * 该方法用于通知线程停止执行
     */
    void toStop();
}

