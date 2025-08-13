package com.ccexid.core.handler;

/**
 * job handler
 *
 * @author xuxueli 2015-12-19 19:06:38
 */
public abstract class IJobHandler implements JobHandler {

    /**
     * 任务执行入口方法
     * <p>
     * 当调度中心触发任务时，框架会调用此方法执行具体业务逻辑
     *
     * @throws Exception 执行过程中发生的异常，由框架统一捕获处理
     */

    public abstract void execute() throws Exception;
}
