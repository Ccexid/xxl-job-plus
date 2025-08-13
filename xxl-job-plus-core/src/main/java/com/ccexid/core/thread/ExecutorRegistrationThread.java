package com.ccexid.core.thread;

import com.ccexid.core.constant.RegisterConstant;
import com.ccexid.core.enums.RegisterTypeEnum;
import com.ccexid.core.enums.ResultCode;
import com.ccexid.core.executor.XxlJobExecutor;
import com.ccexid.core.service.AdminBiz;
import com.ccexid.core.service.model.ApiResponse;
import com.ccexid.core.service.model.RegistryParam;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 执行器注册线程
 * 负责向调度中心注册/移除执行器信息，维持心跳连接
 *
 * @author xuxueli 2017-03-02
 */
public class ExecutorRegistrationThread {
    private static final Logger logger = LoggerFactory.getLogger(ExecutorRegistrationThread.class);
    private static final ExecutorRegistrationThread INSTANCE = new ExecutorRegistrationThread();

    // 单例模式
    public static ExecutorRegistrationThread getInstance() {
        return INSTANCE;
    }

    private Thread registrationThread;
    private volatile boolean isStopping = false;  // 线程停止标志
    /**
     * -- SETTER --
     *  设置调度中心客户端列表（用于解耦，便于测试）
     */
    @Setter
    private List<AdminBiz> adminBizList;  // 调度中心客户端列表（通过注入解耦）

    /**
     * 启动注册线程
     *
     * @param appName 应用名称
     * @param address 执行器地址
     */
    public void start(String appName, String address) {
        // 从执行器获取调度中心客户端列表（通过setter注入后可移除此依赖）
        this.adminBizList = XxlJobExecutor.getAdminClients();

        // 验证配置合法性
        if (!validateConfig(appName)) {
            return;
        }

        // 初始化并启动注册线程
        registrationThread = new Thread(() -> runRegistrationLoop(appName, address),
                "xxl-job-executor-registration-thread");
        registrationThread.setDaemon(true);
        registrationThread.start();
    }

    /**
     * 停止注册线程
     */
    public void stop() {
        isStopping = true;

        // 中断并等待线程结束
        Optional.ofNullable(registrationThread)
                .ifPresent(thread -> {
                    thread.interrupt();
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        logger.error("注册线程停止失败", e);
                    }
                });
    }

    // ---------------------- 核心逻辑 ----------------------

    /**
     * 注册循环主逻辑
     */
    private void runRegistrationLoop(String appName, String address) {
        // 循环注册直到线程停止
        while (!isStopping) {
            try {
                // 执行注册
                RegistryParam param = createRegistryParam(appName, address);
                boolean registered = registerExecutor(param);

                if (registered) {
                    logger.debug("执行器注册成功, 参数: {}", param);
                } else {
                    logger.info("执行器注册失败, 参数: {}", param);
                }
            } catch (Throwable e) {
                if (!isStopping) {
                    logger.error("执行器注册异常", e);
                }
            }

            // 等待下一次注册间隔
            sleepBetweenRegistrations();
        }

        // 线程停止时执行注销
        unregisterExecutor(createRegistryParam(appName, address));
        logger.info("执行器注册线程已销毁");
    }

    /**
     * 执行器注册
     */
    private boolean registerExecutor(RegistryParam param) {
        return adminBizList.stream()
                .anyMatch(adminBiz -> {
                    try {
                        ApiResponse<?> result = adminBiz.registry(param);
                        return result != null && ResultCode.SUCCESS.getCode() == result.getCode();
                    } catch (Throwable e) {
                        logger.info("向调度中心注册失败, 参数: {}", param, e);
                        return false;
                    }
                });
    }

    /**
     * 执行器注销
     */
    private void unregisterExecutor(RegistryParam param) {
        adminBizList.stream()
                .anyMatch(adminBiz -> {
                    try {
                        ApiResponse<?> result = adminBiz.deregister(param);
                        if (result != null &&  ResultCode.SUCCESS.getCode() == result.getCode()) {
                            logger.info("执行器注销成功, 参数: {}", param);
                            return true;
                        } else {
                            logger.info("执行器注销失败, 参数: {}, 结果: {}", param, result);
                            return false;
                        }
                    } catch (Throwable e) {
                        if (!isStopping) {
                            logger.info("向调度中心注销失败, 参数: {}", param, e);
                        }
                        return false;
                    }
                });
    }

    // ---------------------- 工具方法 ----------------------

    /**
     * 验证配置合法性
     */
    private boolean validateConfig(String appName) {
        if (appName == null || appName.trim().isEmpty()) {
            logger.warn("执行器注册失败: 应用名称为空");
            return false;
        }
        if (adminBizList == null || adminBizList.isEmpty()) {
            logger.warn("执行器注册失败: 调度中心地址列表为空");
            return false;
        }
        return true;
    }

    /**
     * 创建注册参数
     */
    private RegistryParam createRegistryParam(String appName, String address) {
        return new RegistryParam(RegisterTypeEnum.EXECUTOR.name(), appName, address);
    }

    /**
     * 注册间隔等待
     */
    private void sleepBetweenRegistrations() {
        try {
            if (!isStopping) {
                TimeUnit.SECONDS.sleep(RegisterConstant.BEAT_TIMEOUT);
            }
        } catch (InterruptedException e) {
            if (!isStopping) {
                logger.warn("注册线程等待被中断", e);
            }
        }
    }

    // ---------------------- 依赖注入（可选） ----------------------

}
