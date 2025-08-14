package com.ccexid.core.biz;

import com.ccexid.core.model.IdleBeatParam;
import com.ccexid.core.model.KillParam;
import com.ccexid.core.model.LogParam;
import com.ccexid.core.model.LogResult;
import com.ccexid.core.model.ResponseEntity;
import com.ccexid.core.model.TriggerParam;

/**
 * 执行器接口
 * 定义了执行器需要实现的核心业务方法，包括心跳检测、空闲检测、任务执行、任务终止和日志查询等功能
 *
 * @author xuxueli 2017-03-01
 */
public interface ExecutorBiz {

    /**
     * 心跳检测方法
     * 用于检测执行器是否在线
     *
     * @return 响应实体，包含检测结果信息
     */
    ResponseEntity<String> beat();

    /**
     * 空闲检测方法
     * 检测指定任务是否正在运行，用于任务调度前的准备工作
     *
     * @param idleBeatParam 空闲检测参数，包含任务ID
     * @return 响应实体，包含检测结果信息
     */
    ResponseEntity<String> idleBeat(IdleBeatParam idleBeatParam);

    /**
     * 任务执行方法
     * 触发指定任务的执行
     *
     * @param triggerParam 任务触发参数，包含任务执行所需的各种配置信息
     * @return 响应实体，包含执行结果信息
     */
    ResponseEntity<String> run(TriggerParam triggerParam);

    /**
     * 任务终止方法
     * 终止正在执行的任务
     *
     * @param killParam 任务终止参数，包含需要终止的任务ID
     * @return 响应实体，包含终止结果信息
     */
    ResponseEntity<String> kill(KillParam killParam);

    /**
     * 日志查询方法
     * 查询任务执行日志
     *
     * @param logParam 日志查询参数，包含日志时间、ID和起始行号等信息
     * @return 响应实体，包含日志查询结果
     */
    ResponseEntity<LogResult> log(LogParam logParam);

}
