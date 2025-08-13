package com.ccexid.core.biz.client;


import com.ccexid.core.biz.ExecutorBiz;
import com.ccexid.core.biz.model.*;
import com.ccexid.core.utils.XxlJobRemoteUtils;
import lombok.Data;

/**
 * admin api test
 *
 * @author xuxueli 2017-07-28 22:14:52
 */
@Data
public class ExecutorBizClient implements ExecutorBiz {

    public ExecutorBizClient() {
    }
    public ExecutorBizClient(String addressUrl, String accessToken, int timeout) {
        this.addressUrl = addressUrl;
        this.accessToken = accessToken;
        this.timeout = timeout;

        // valid
        if (!this.addressUrl.endsWith("/")) {
            this.addressUrl = this.addressUrl + "/";
        }
        if (!(this.timeout >=1 && this.timeout <= 10)) {
            this.timeout = 3;
        }
    }

    private String addressUrl ;
    private String accessToken;
    private int timeout;

    @Override
    public ApiResponse<?>  beat() {
        return XxlJobRemoteUtils.postJson(addressUrl+"beat", accessToken, timeout, "", String.class);
    }

    @Override
    public ApiResponse<?>  idleBeat(IdleBeatParam idleBeatParam){
        return XxlJobRemoteUtils.postJson(addressUrl+"idleBeat", accessToken, timeout, idleBeatParam, String.class);
    }

    @Override
    public ApiResponse<?>  run(TriggerParam triggerParam) {
        return XxlJobRemoteUtils.postJson(addressUrl + "run", accessToken, timeout, triggerParam, String.class);
    }

    @Override
    public ApiResponse<?>  kill(KillParam killParam) {
        return XxlJobRemoteUtils.postJson(addressUrl + "kill", accessToken, timeout, killParam, String.class);
    }

    @Override
    public ApiResponse<LogResult> log(LogParam logParam) {
        return XxlJobRemoteUtils.postJson(addressUrl + "log", accessToken, timeout, logParam, LogResult.class);
    }


}
