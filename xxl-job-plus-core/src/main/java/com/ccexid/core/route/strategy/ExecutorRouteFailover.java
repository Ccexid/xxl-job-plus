package com.ccexid.core.route.strategy;


import com.ccexid.core.model.ResponseEntity;
import com.ccexid.core.model.TriggerParam;
import com.ccexid.core.route.ExecutorRouter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Created by xuxueli on 17/3/10.
 */
@Slf4j
public  class ExecutorRouteFailover extends ExecutorRouter {

    /**
     * route address
     *
     * @param triggerParam
     * @param addressList  执行器地址
     * @return ReturnT.content=address
     */
    @Override
    public ResponseEntity<String> route(TriggerParam triggerParam, List<String> addressList) {
        return null;
    }
}
