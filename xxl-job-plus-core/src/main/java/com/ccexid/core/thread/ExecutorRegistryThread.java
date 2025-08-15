package com.ccexid.core.thread;

import com.ccexid.core.biz.AdminBiz;
import com.ccexid.core.constant.RegisterConstant;
import com.ccexid.core.enums.RegisterType;
import com.ccexid.core.executor.JobExecutor;
import com.ccexid.core.model.RegistryParam;
import com.ccexid.core.model.ResponseEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 执行器注册线程类
 * 负责执行器的注册与注销操作
 *
 * @author xuxueli 2017-03-02
 */
@Slf4j
public class ExecutorRegistryThread implements IThread {

    private static final ExecutorRegistryThread INSTANCE = new ExecutorRegistryThread();

    public static ExecutorRegistryThread getInstance() {
        return INSTANCE;
    }

    private Thread registryThread;
    private volatile boolean toStop = false;
    private String appName;
    private String address;

    /**
     * 启动执行器注册线程
     *
     * @param appName 应用名称
     * @param address 注册地址
     */
    public void start(final String appName, final String address) {

        // 参数校验
        if (StringUtils.isBlank(appName)) {
            log.warn(">>>>>>>>>>> xxl-job, executor registry config fail, appName is null.");
            return;
        }
        this.appName = appName;
        this.address = address;
    }

    @Override
    public void start() {
        List<AdminBiz> adminBizList = JobExecutor.getAdminBizList();
        if (adminBizList == null || adminBizList.isEmpty()) {
            log.warn(">>>>>>>>>>> xxl-job, executor registry config fail, adminBizList is null or empty.");
            return;
        }
        registryThread = new Thread(() -> {
            // 注册执行器
            while (!toStop) {
                try {
                    RegistryParam registryParam = new RegistryParam(RegisterType.EXECUTOR.name(), appName, address);
                    boolean registrySuccess = false;
                    for (AdminBiz adminBiz : adminBizList) {
                        try {
                            ResponseEntity<String> registryResult = adminBiz.registry(registryParam);
                            if (registryResult != null && ResponseEntity.isSuccess(registryResult)) {
                                log.debug(">>>>>>>>>>> xxl-job registry success, registryParam:{}, registryResult:{}", registryParam, registryResult);
                                registrySuccess = true;
                                break;
                            } else {
                                log.info(">>>>>>>>>>> xxl-job registry fail, registryParam:{}, registryResult:{}", registryParam, registryResult);
                            }
                        } catch (Exception e) {
                            log.info(">>>>>>>>>>> xxl-job registry error, registryParam:{}", registryParam, e);
                        }
                    }

                    // 如果注册失败且未停止，则记录日志
                    if (!registrySuccess && !toStop) {
                        log.warn(">>>>>>>>>>> xxl-job registry failed for all admin addresses, registryParam:{}", registryParam);
                    }
                } catch (Exception e) {
                    if (!toStop) {
                        log.error(">>>>>>>>>>> xxl-job registry exception", e);
                    }
                }

                // 等待下次注册
                try {
                    if (!toStop) {
                        TimeUnit.SECONDS.sleep(RegisterConstant.BEAT_TIMEOUT);
                    }
                } catch (InterruptedException e) {
                    if (!toStop) {
                        log.warn(">>>>>>>>>>> xxl-job, executor registry thread interrupted, error msg:{}", e.getMessage());
                        Thread.currentThread().interrupt();
                    }
                }
            }

            // 注销执行器
            try {
                RegistryParam registryParam = new RegistryParam(RegisterType.EXECUTOR.name(), appName, address);
                boolean deregisterSuccess = false;
                for (AdminBiz adminBiz : adminBizList) {
                    try {
                        ResponseEntity<String> registryResult = adminBiz.deregister(registryParam);
                        if (registryResult != null && ResponseEntity.isSuccess(registryResult)) {
                            log.info(">>>>>>>>>>> xxl-job registry-remove success, registryParam:{}, registryResult:{}", registryParam, registryResult);
                            deregisterSuccess = true;
                            break;
                        } else {
                            log.info(">>>>>>>>>>> xxl-job registry-remove fail, registryParam:{}, registryResult:{}", registryParam, registryResult);
                        }
                    } catch (Exception e) {
                        if (!toStop) {
                            log.error(">>>>>>>>>>> xxl-job registry-remove error, registryParam:{}", registryParam, e);
                        }
                    }
                }

                // 如果注销失败且未停止，则记录日志
                if (!deregisterSuccess && !toStop) {
                    log.warn(">>>>>>>>>>> xxl-job deregistry failed for all admin addresses, registryParam:{}", registryParam);
                }
            } catch (Exception e) {
                if (!toStop) {
                    log.error(">>>>>>>>>>> xxl-job deregister exception", e);
                }
            }
            log.info(">>>>>>>>>>> xxl-job, executor registry thread destroy.");

        }, "xxl-job, executor ExecutorRegistryThread");
        registryThread.setDaemon(true);
        registryThread.start();
    }

    @Override
    public void toStop() {
        toStop = true;

        // interrupt and wait
        if (registryThread != null && registryThread.isAlive()) {
            registryThread.interrupt();
            try {
                registryThread.join();
            } catch (InterruptedException e) {
                log.error(">>>>>>>>>>> xxl-job, executor registry thread join error", e);
                Thread.currentThread().interrupt();
            }
        }
    }
}
