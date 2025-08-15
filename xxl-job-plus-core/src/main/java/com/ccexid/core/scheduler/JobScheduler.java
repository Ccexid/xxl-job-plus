package com.ccexid.core.scheduler;

import com.ccexid.core.biz.ExecutorBiz;
import com.ccexid.core.biz.client.ExecutorBizClient;
import com.ccexid.core.configuration.JobPlusManagerAutoConfiguration;
import com.ccexid.core.exception.JobSchedulerException;
import com.ccexid.core.props.JobPlusManagerProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class JobScheduler {
    private static final ConcurrentMap<String, ExecutorBiz> executorBizRepository = new ConcurrentHashMap<>();

    /**
     * 根据地址获取执行器业务客户端实例
     *
     * @param address 执行器地址，如果为空白字符串则返回null
     * @return ExecutorBiz 执行器业务客户端实例
     * @throws Exception 创建执行器业务客户端时发生的异常
     */
    public static ExecutorBiz getExecutorBiz(String address) throws Exception {
        // valid
        if (StringUtils.isBlank(address)) {
            return null;
        }

        address = address.trim();

        // 使用 computeIfAbsent 原子性地处理缓存加载
        return executorBizRepository.computeIfAbsent(address, addr -> {
            try {
                JobPlusManagerAutoConfiguration adminConfig = JobPlusManagerAutoConfiguration.getAdminConfig();
                if (adminConfig == null) {
                    log.warn("AdminConfig is null");
                    throw new JobSchedulerException("AdminConfig is null");
                }
                JobPlusManagerProperties plusManagerConfig = adminConfig.plusManagerConfig();
                if (plusManagerConfig == null) {
                    log.warn("PlusManagerConfig is null");
                    throw new JobSchedulerException("PlusManagerConfig is null");
                }

                return new ExecutorBizClient(addr,
                        plusManagerConfig.getAccessToken(),
                        plusManagerConfig.getTimeout());
            } catch (Exception e) {
                throw new RuntimeException("Failed to create ExecutorBiz for address: " + addr, e);
            }
        });
    }


    public void init() throws Exception {
        // 初始化逻辑可以在这里补充
    }

    public void destroy() throws Exception {
        // 销毁逻辑可以在这里补充
        executorBizRepository.clear();
    }
}
