package com.ccexid.core.biz.model;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by xuxueli on 16/7/22.
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


    @Override
    public String toString() {
        return "TriggerParam{" +
                "jobId=" + jobId +
                ", executorHandler='" + executorHandler + '\'' +
                ", executorParams='" + executorParams + '\'' +
                ", executorBlockStrategy='" + executorBlockStrategy + '\'' +
                ", executorTimeout=" + executorTimeout +
                ", logId=" + logId +
                ", logDateTime=" + logDateTime +
                ", glueType='" + glueType + '\'' +
                ", glueSource='" + glueSource + '\'' +
                ", glueUpdateTime=" + glueUpdateTime +
                ", broadcastIndex=" + broadcastIndex +
                ", broadcastTotal=" + broadcastTotal +
                '}';
    }

}
