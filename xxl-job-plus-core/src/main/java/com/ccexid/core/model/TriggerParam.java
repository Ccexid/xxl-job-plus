package com.ccexid.core.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 任务触发参数类，封装了调度任务执行时需要的各种参数信息
 * 
 * @author xuxueli 16/7/22
 */
@Data
public class TriggerParam implements Serializable {
    private static final long serialVersionUID = 42L;

    /**
     * 任务ID
     */
    private int jobId;

    /**
     * 执行器处理器名称
     */
    private String executorHandler;
    /**
     * 执行器参数
     */
    private String executorParams;
    /**
     * 执行器阻塞策略
     */
    private String executorBlockStrategy;
    /**
     * 执行器超时时间
     */
    private int executorTimeout;

    /**
     * 日志ID
     */
    private long logId;
    /**
     * 日志日期时间
     */
    private long logDateTime;

    /**
     * GLUE类型
     */
    private String glueType;
    /**
     * GLUE源代码
     */
    private String glueSource;
    /**
     * GLUE更新时间
     */
    private long glueUpdateTime;

    /**
     * 广播索引
     */
    private int broadcastIndex;
    /**
     * 广播总数
     */
    private int broadcastTotal;

    /**
     * 无参构造函数
     */
    public TriggerParam() {
    }

    /**
     * 有参构造函数，用于初始化任务触发参数
     *
     * @param jobId                  任务ID
     * @param executorHandler        执行器处理器名称
     * @param executorParams         执行器参数
     * @param executorBlockStrategy  执行器阻塞策略
     * @param executorTimeout        执行器超时时间
     * @param logId                  日志ID
     * @param logDateTime            日志日期时间
     * @param glueType               GLUE类型
     * @param glueSource             GLUE源代码
     * @param glueUpdateTime         GLUE更新时间
     * @param broadcastIndex         广播索引
     * @param broadcastTotal         广播总数
     */
    public TriggerParam(int jobId, String executorHandler, String executorParams, String executorBlockStrategy, int executorTimeout, long logId, long logDateTime, String glueType, String glueSource, long glueUpdateTime, int broadcastIndex, int broadcastTotal) {
        this.jobId = jobId;
        this.executorHandler = executorHandler;
        this.executorParams = executorParams;
        this.executorBlockStrategy = executorBlockStrategy;
        this.executorTimeout = executorTimeout;
        this.logId = logId;
        this.logDateTime = logDateTime;
        this.glueType = glueType;
        this.glueSource = glueSource;
        this.glueUpdateTime = glueUpdateTime;
        this.broadcastIndex = broadcastIndex;
        this.broadcastTotal = broadcastTotal;
    }
}