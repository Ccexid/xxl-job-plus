package com.ccexid.core.biz;


import com.ccexid.core.model.HandleCallbackParam;
import com.ccexid.core.model.RegistryParam;
import com.ccexid.core.model.ResponseEntity;

import java.util.List;

/**
 * 管理端业务接口
 *
 * @author xuxueli 2017-07-27 21:52:49
 */
public interface AdminBiz {

    /**
     * 处理执行器回调结果
     *
     * @param callbackParamList 回调参数列表
     * @return 响应结果
     */
    ResponseEntity<String> callback(List<HandleCallbackParam> callbackParamList);

    /**
     * 执行器注册
     *
     * @param registryParam 注册参数
     * @return 响应结果
     */
    ResponseEntity<String> registry(RegistryParam registryParam);

    /**
     * 执行器注销
     *
     * @param registryParam 注册参数
     * @return 响应结果
     */
    ResponseEntity<String> deregister(RegistryParam registryParam);
}
