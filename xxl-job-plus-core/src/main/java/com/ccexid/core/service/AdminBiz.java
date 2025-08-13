package com.ccexid.core.service;

import com.ccexid.core.service.model.ApiResponse;
import com.ccexid.core.service.model.HandleCallbackParam;
import com.ccexid.core.service.model.RegistryParam;

import java.util.List;

/**
 * 管理服务接口，提供回调、注册和注销等管理功能
 */
public interface AdminBiz {

    /**
     * 处理回调请求
     *
     * @param callbackParamList 回调参数列表，包含需要处理的回调信息
     * @return ApiResponse<?> 返回处理结果的API响应对象
     */
    ApiResponse<?> callback(List<HandleCallbackParam> callbackParamList);

    /**
     * 注册服务或组件
     *
     * @param registryParam 注册参数，包含注册所需的信息
     * @return ApiResponse<?> 返回注册结果的API响应对象
     */
    ApiResponse<?> registry(RegistryParam registryParam);

    /**
     * 注销服务或组件
     *
     * @param registryParam 注销参数，包含注销所需的信息
     * @return ApiResponse<?> 返回注销结果的API响应对象
     */
    ApiResponse<?> deregister(RegistryParam registryParam);
}

