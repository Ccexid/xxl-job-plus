package com.ccexid.core.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 处理回调参数实体类
 * 用于封装任务执行完成后的回调信息，包括日志ID、执行结果码和执行消息等
 */
@Data
public class HandleCallbackParam implements Serializable {
    private static final long serialVersionUID = 42L;

    /**
     * 日志ID
     */
    private long logId;

    /**
     * 日志日期时间戳
     */
    private long logDateTim;

    /**
     * 处理结果码
     * 用于标识任务执行的结果状态
     */
    private int handleCode;

    /**
     * 处理消息
     * 任务执行后的结果描述信息
     */
    private String handleMsg;

    /**
     * 构造函数
     *
     * @param logId      日志ID
     * @param logDateTim 日志日期时间戳
     * @param handleCode 处理结果码
     * @param handleMsg  处理消息
     */
    public HandleCallbackParam(long logId, long logDateTim, int handleCode, String handleMsg) {
        this.logId = logId;
        this.logDateTim = logDateTim;
        this.handleCode = handleCode;
        this.handleMsg = handleMsg;
    }

    /**
     * 无参构造函数
     */
    public HandleCallbackParam() {
    }
}
