package com.ccexid.core.service.client;

import com.ccexid.core.service.AdminBiz;
import com.ccexid.core.service.model.ApiResponse;
import com.ccexid.core.service.model.HandleCallbackParam;
import com.ccexid.core.service.model.RegistryParam;
import com.ccexid.core.utils.XxlJobRemoteUtils;
import lombok.Data;

import java.util.List;

/**
 * admin api test
 *
 * @author xuxueli 2017-07-28 22:14:52
 */
@Data
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
     * 处理回调请求
     *
     * @param callbackParamList 回调参数列表，包含需要处理的回调信息
     * @return ApiResponse<?> 返回处理结果的API响应对象
     */
    @Override
    public ApiResponse<?> callback(List<HandleCallbackParam> callbackParamList) {
        return XxlJobRemoteUtils.postJson(addressUrl + "api/callback", accessToken, timeout, callbackParamList, String.class);
    }

    /**
     * 注册服务或组件
     *
     * @param registryParam 注册参数，包含注册所需的信息
     * @return ApiResponse<?> 返回注册结果的API响应对象
     */
    @Override
    public ApiResponse<?> registry(RegistryParam registryParam) {
        return XxlJobRemoteUtils.postJson(addressUrl + "api/registry", accessToken, timeout, registryParam, String.class);
    }

    /**
     * 注销服务或组件
     *
     * @param registryParam 注销参数，包含注销所需的信息
     * @return ApiResponse<?> 返回注销结果的API响应对象
     */
    @Override
    public ApiResponse<?> deregister(RegistryParam registryParam) {
        return XxlJobRemoteUtils.postJson(addressUrl + "api/registryRemove", accessToken, timeout, registryParam, String.class);
    }
}
