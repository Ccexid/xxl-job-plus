package com.ccexid.core.handler;

/**
 * 作业处理器接口
 * 定义了作业处理器的生命周期管理方法，包括初始化和销毁操作
 */
public interface IJobHandler {
    /**
     * 初始化作业处理器
     * 在作业处理器开始执行前调用，用于执行必要的初始化操作
     *
     * @throws Exception 初始化过程中可能抛出的异常
     */
    void init() throws Exception;

    /**
     * 销毁作业处理器
     * 在作业处理器结束时调用，用于释放资源和执行清理操作
     *
     * @throws Exception 销毁过程中可能抛出的异常
     */
    void destroy() throws Exception;
}

