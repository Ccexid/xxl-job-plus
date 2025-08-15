package com.ccexid.admin.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * JobPlusLog类用于存储任务执行的详细日志信息
 * 包含任务的基本信息、执行信息、触发信息、处理信息和告警状态等
 */
@Data
@TableName("xxl_job_log")
public class JobPlusLog {

    /**
     * 日志记录的唯一标识ID
     */
    private Long id;

    // job info
    /**
     * 任务分组ID，用于区分不同的任务分组
     */
    private Integer jobGroup;

    /**
     * 任务ID，标识具体的任务
     */
    private Integer jobId;

    // execute info
    /**
     * 执行器地址，指定任务执行的具体服务器地址
     */
    private String executorAddress;

    /**
     * 执行器处理器，指定处理任务的具体处理器名称
     */
    private String executorHandler;

    /**
     * 执行器参数，传递给执行器的参数信息
     */
    private String executorParam;

    /**
     * 执行器分片参数，用于任务分片执行时的参数配置
     */
    private String executorShardingParam;

    /**
     * 执行器失败重试次数，当任务执行失败时的重试次数设置
     */
    private Integer executorFailRetryCount;

    // trigger info
    /**
     * 任务触发时间，记录任务被触发执行的具体时间
     */
    private Date triggerTime;

    /**
     * 触发状态码，标识任务触发的结果状态
     */
    private Integer triggerCode;

    /**
     * 触发消息，记录任务触发过程中的详细信息或错误信息
     */
    private String triggerMsg;

    // handle info
    /**
     * 任务处理时间，记录任务开始处理的时间
     */
    private Date handleTime;

    /**
     * 处理状态码，标识任务处理的结果状态
     */
    private Integer handleCode;

    /**
     * 处理消息，记录任务处理过程中的详细信息或错误信息
     */
    private String handleMsg;

    // alarm info
    /**
     * 告警状态，标识是否触发了告警机制
     */
    private Integer alarmStatus;
}

