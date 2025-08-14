package com.ccexid.core.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 日志参数实体类
 * 用于封装日志查询请求的参数信息，包括日志日期时间、日志ID和起始行号
 *
 * @author xuxueli 2020-04-11 22:27
 */
@Data
public class LogParam implements Serializable {
    private static final long serialVersionUID = 42L;

    /**
     * 无参构造函数
     */
    public LogParam() {
    }

    /**
     * 构造函数
     *
     * @param logDateTim  日志日期时间戳
     * @param logId       日志ID
     * @param fromLineNum 起始行号
     */
    public LogParam(long logDateTim, long logId, int fromLineNum) {
        this.logDateTim = logDateTim;
        this.logId = logId;
        this.fromLineNum = fromLineNum;
    }

    /**
     * 日志日期时间戳
     */
    private long logDateTim;

    /**
     * 日志ID
     */
    private long logId;

    /**
     * 起始行号
     * 从指定行号开始读取日志内容
     */
    private int fromLineNum;

}
