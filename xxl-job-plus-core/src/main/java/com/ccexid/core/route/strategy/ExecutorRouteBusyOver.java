package com.ccexid.core.route.strategy;


import com.ccexid.core.biz.ExecutorBiz;
import com.ccexid.core.enums.ResponseCode;
import com.ccexid.core.model.IdleBeatParam;
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
public class ExecutorRouteBusyOver extends ExecutorRouter {
    /**
     * route address
     *
     * @param triggerParam
     * @param addressList  执行器地址
     * @return ReturnT.content=address
     */
    @Override
    public ResponseEntity<String> route(TriggerParam triggerParam, List<String> addressList) {
        StringBuffer idleBeatResultSB = new StringBuffer();
        for (String address : addressList) {
            // beat
            ResponseEntity<String> idleBeatResult = null;
            try {
                ExecutorBiz executorBiz = JobScheduler.getExecutorBiz(address);
                assert executorBiz != null;
                idleBeatResult = executorBiz.idleBeat(new IdleBeatParam(triggerParam.getJobId()));
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                idleBeatResult = ResponseEntity.of(ResponseCode.INTERNAL_SERVER_ERROR.getCode(), e.getMessage(), null);
            }
            idleBeatResultSB.append((idleBeatResultSB.length() > 0) ? "<br><br>" : "")
                    .append("Idle check：")
                    .append("<br>address：").append(address)
                    .append("<br>code：").append(idleBeatResult.getCode())
                    .append("<br>msg：").append(idleBeatResult.getMsg());

            // beat success
            if (ResponseEntity.isSuccess(idleBeatResult)) {
                idleBeatResult.setMsg(idleBeatResultSB.toString());
                idleBeatResult.setContent(address);
                return idleBeatResult;
            }
        }
        return ResponseEntity.of(ResponseCode.INTERNAL_SERVER_ERROR, idleBeatResultSB.toString());
    }
}
