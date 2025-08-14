package com.ccexid.core.biz.client;


import com.ccexid.core.biz.AdminBiz;
import com.ccexid.core.model.HandleCallbackParam;
import com.ccexid.core.model.RegistryParam;
import com.ccexid.core.model.ResponseEntity;
import com.ccexid.core.util.XxlJobRemotingUtil;

import java.util.List;

/**
 * admin api test
 *
 * @author xuxueli 2017-07-28 22:14:52
 */
public class AdminBizClient implements AdminBiz {

    public AdminBizClient() {
    }

    public AdminBizClient(String addressUrl, String accessToken, int timeout) {
        this.addressUrl = addressUrl;
        this.accessToken = accessToken;
        this.timeout = timeout;

        // valid
        if (!this.addressUrl.endsWith("/")) {
            this.addressUrl = this.addressUrl + "/";
        }
        if (!(this.timeout >= 1 && this.timeout <= 10)) {
            this.timeout = 3;
        }
    }

    private String addressUrl;
    private String accessToken;
    private int timeout;


    /**
     * 处理执行器回调结果
     *
     * @param callbackParamList 回调参数列表
     * @return 响应结果
     */
    @Override
    public ResponseEntity<String> callback(List<HandleCallbackParam> callbackParamList) {
        return XxlJobRemotingUtil.postBody(addressUrl + "api/callback", accessToken, timeout, callbackParamList, String.class);
    }

    /**
     * 执行器注册
     *
     * @param registryParam 注册参数
     * @return 响应结果
     */
    @Override
    public ResponseEntity<String> registry(RegistryParam registryParam) {
        return XxlJobRemotingUtil.postBody(addressUrl + "api/registry", accessToken, timeout, registryParam, String.class);
    }

    /**
     * 执行器注销
     *
     * @param registryParam 注册参数
     * @return 响应结果
     */
    @Override
    public ResponseEntity<String> deregister(RegistryParam registryParam) {
        return XxlJobRemotingUtil.postBody(addressUrl + "api/registryRemove", accessToken, timeout, registryParam, String.class);
    }
}
