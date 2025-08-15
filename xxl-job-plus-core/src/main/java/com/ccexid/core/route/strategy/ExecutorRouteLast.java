package com.ccexid.core.route.strategy;

import com.ccexid.core.model.ResponseEntity;
import com.ccexid.core.model.TriggerParam;
import com.ccexid.core.route.ExecutorRouter;

import java.util.List;

public class ExecutorRouteLast extends ExecutorRouter {
    /**
     * route address
     *
     * @param triggerParam 触发参数
     * @param addressList  执行器地址
     * @return ReturnT.content=address
     */
    @Override
    public ResponseEntity<String> route(TriggerParam triggerParam, List<String> addressList) {
        return ResponseEntity.success(addressList.get(addressList.size() - 1));
    }
}
