package com.ccexid.core.server;

import com.ccexid.core.thread.ExecutorRegistrationThread;

/**
 * 注册中心管理器
 * 负责与注册中心的交互
 */
public class RegistrationManager {

    /**
     * 启动注册线程
     */
    public void startRegistration(final String appName, final String address) {
        ExecutorRegistrationThread.getInstance().start(appName, address);
    }

    /**
     * 停止注册线程
     */
    public void stopRegistration() {
        ExecutorRegistrationThread.getInstance().stop();
    }
}