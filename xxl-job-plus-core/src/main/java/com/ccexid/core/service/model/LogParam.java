package com.ccexid.core.service.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 日志参数模型类
 * 用于封装日志查询相关的参数信息
 *
 * @author xuxueli 2020-04-11 22:27
 */
@Data
public class LogParam implements Serializable {
    private static final long serialVersionUID = 42L;

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
     */
    private int fromLineNum;

}
