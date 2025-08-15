package com.ccexid.core.scheduler;

import com.ccexid.core.biz.ExecutorBiz;
import com.ccexid.core.biz.client.ExecutorBizClient;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class AbstractXxlJobScheduler {
    private static final ConcurrentMap<String, ExecutorBiz> executorBizRepository = new ConcurrentHashMap<>();

    public static ExecutorBiz getExecutorBiz(String address) throws Exception {
        // valid
        if (StringUtils.isBlank(address)) {
            return null;
        }
        // load-cache
        address = address.trim();
        return executorBizRepository.computeIfAbsent(address, addr -> {
            try {
                return new ExecutorBizClient(addr, "", 30);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
