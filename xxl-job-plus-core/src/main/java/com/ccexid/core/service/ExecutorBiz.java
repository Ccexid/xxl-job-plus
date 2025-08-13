package com.ccexid.core.service;

import com.ccexid.core.service.model.*;

/**
 * ExecutorService接口定义了执行器服务的核心功能方法
 * 该接口提供了任务执行、状态检查、任务终止和日志查询等操作
 */
public interface ExecutorBiz {

    /**
     * 执行心跳检测，用于检查执行器是否存活
     *
     * @return ApiResponse<?> 包含心跳检测结果的响应对象
     */
    ApiResponse<?> beat();

    /**
     * 执行空闲状态检测，用于检查执行器是否处于空闲状态
     *
     * @return ApiResponse<?> 包含空闲状态检测结果的响应对象
     */
    ApiResponse<?> idleBeat(IdleBeatParam idleBeatParam);

    /**
     * 执行任务触发操作，根据触发参数启动相应的任务执行
     *
     * @param triggerParam 任务触发参数，包含任务执行所需的信息
     * @return ApiResponse<?> 包含任务执行结果的响应对象
     */
    ApiResponse<?> run(TriggerParam triggerParam);

    /**
     * 终止指定任务的执行
     *
     * @param killParam 任务终止参数，包含需要终止的任务信息
     * @return ApiResponse<?> 包含任务终止结果的响应对象
     */
    ApiResponse<?> kill(KillParam killParam);

    /**
     * 查询任务执行日志
     *
     * @param logParam 日志查询参数，包含日志查询的条件信息
     * @return ApiResponse<LogResult> 包含日志查询结果的响应对象
     */
    ApiResponse<LogResult> log(LogParam logParam);
}

