package com.ccexid.core.route.strategy;


import com.ccexid.core.model.ResponseEntity;
import com.ccexid.core.model.TriggerParam;
import com.ccexid.core.route.ExecutorRouter;

import java.util.List;
import java.util.Random;

/**
 * Created by xuxueli on 17/3/10.
 */
public class ExecutorRouteRandom extends ExecutorRouter {

    private static Random localRandom = new Random();

    @Override
    public ResponseEntity<String> route(TriggerParam triggerParam, List<String> addressList) {
        String address = addressList.get(localRandom.nextInt(addressList.size()));
        return ResponseEntity.success(address);
    }

}
