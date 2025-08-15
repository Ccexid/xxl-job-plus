package com.ccexid.core.route.strategy;

import com.ccexid.core.model.ResponseEntity;
import com.ccexid.core.model.TriggerParam;
import com.ccexid.core.route.ExecutorRouter;

import java.util.List;

public class ExecutorRouteFirst extends ExecutorRouter {
    /**
     * route address
     *
     * @param triggerParam
     * @param addressList
     * @return ReturnT.content=address
     */
    @Override
    public ResponseEntity<String> route(TriggerParam triggerParam, List<String> addressList) {
        return ResponseEntity.success(addressList.get(0));
    }
}
