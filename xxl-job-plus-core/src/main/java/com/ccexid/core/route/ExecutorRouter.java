package com.ccexid.core.route;

import com.ccexid.core.model.ResponseEntity;
import com.ccexid.core.model.TriggerParam;

import java.util.List;

/**
 * Created by xuxueli on 17/3/10.
 */
public abstract class ExecutorRouter {
    /**
     * route address
     *
     * @param addressList  执行器地址
     * @return  ReturnT.content=address
     */
    public abstract ResponseEntity<String> route(TriggerParam triggerParam, List<String> addressList);

}
