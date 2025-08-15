package com.ccexid.core.route.strategy;


import com.ccexid.core.biz.ExecutorBiz;
import com.ccexid.core.enums.ResponseCode;
import com.ccexid.core.model.ResponseEntity;
import com.ccexid.core.model.TriggerParam;
import com.ccexid.core.route.ExecutorRouter;
import com.ccexid.core.scheduler.JobScheduler;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Created by xuxueli on 17/3/10.
 */
@Slf4j
public class ExecutorRouteFailover extends ExecutorRouter {

    /**
     * route address
     *
     * @param triggerParam
     * @param addressList  执行器地址
     * @return ReturnT.content=address
     */
    @Override
    public ResponseEntity<String> route(TriggerParam triggerParam, List<String> addressList) {
        StringBuffer beatResultSB = new StringBuffer();
        for (String address : addressList) {
            // beat
            ResponseEntity<String> beatResult = null;
            try {
                ExecutorBiz executorBiz = JobScheduler.getExecutorBiz(address);
                assert executorBiz != null;
                beatResult = executorBiz.beat();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                beatResult = ResponseEntity.of(ResponseCode.INTERNAL_SERVER_ERROR.getCode(), e.getMessage(), null);
            }
            beatResultSB.append((beatResultSB.length() > 0) ? "<br><br>" : "")
                    .append("Heartbeats：")
                    .append("<br>address：").append(address)
                    .append("<br>code：").append(beatResult.getCode())
                    .append("<br>msg：").append(beatResult.getMsg());

            // beat success
            if (ResponseEntity.isSuccess(beatResult)) {

                beatResult.setMsg(beatResultSB.toString());
                beatResult.setContent(address);
                return beatResult;
            }
        }
        return ResponseEntity.of(ResponseCode.INTERNAL_SERVER_ERROR, beatResultSB.toString());
    }
}
