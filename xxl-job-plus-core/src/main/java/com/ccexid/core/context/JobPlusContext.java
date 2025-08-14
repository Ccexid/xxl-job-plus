package com.ccexid.core.context;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.ccexid.core.enums.ResponseCode;
import lombok.Data;

/**
 * JobPlus任务上下文
 * 用于存储任务执行过程中的上下文信息，包括任务参数、分片信息和执行结果等
 */
@Data
public class JobPlusContext {

    private static TransmittableThreadLocal<JobPlusContext> contextHolder = new TransmittableThreadLocal<>();

    /**
     * 任务ID
     */
    private final long jobId;

    /**
     * 任务参数
     */
    private final String jobParam;

    /**
     * 任务日志文件名
     */
    private final String jobLogFileName;

    /**
     * 分片索引
     * 从0开始的分片索引号
     */
    private final int shardIndex;

    /**
     * 分片总数
     */
    private final int shardTotal;

    /**
     * 处理消息
     * 任务执行后的结果消息
     */
    private String handleMsg;

    /**
     * 处理码
     * 任务执行结果的状态码
     */
    private int handleCode;

    /**
     * 构造函数
     *
     * @param jobId          任务ID
     * @param jobParam       任务参数
     * @param jobLogFileName 任务日志文件名
     * @param shardIndex     分片索引
     * @param shardTotal     分片总数
     */
    public JobPlusContext(long jobId, String jobParam, String jobLogFileName, int shardIndex, int shardTotal) {
        this.jobId = jobId;
        this.jobParam = jobParam;
        this.jobLogFileName = jobLogFileName;
        this.shardIndex = shardIndex;
        this.shardTotal = shardTotal;
        this.handleCode = ResponseCode.SUCCESS.getCode();
    }

    public static void setJobContext(JobPlusContext xxlJobContext){
        contextHolder.set(xxlJobContext);
    }

    public static JobPlusContext getInstance(){
        return contextHolder.get();
    }
}
