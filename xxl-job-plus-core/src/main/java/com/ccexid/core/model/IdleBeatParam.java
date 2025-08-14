package com.ccexid.core.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 空闲检测参数实体类
 * 用于封装空闲检测请求的参数信息，主要包含任务ID
 *
 * @author xuxueli 2020-04-11 22:27
 */
@Data
public class IdleBeatParam implements Serializable {
    private static final long serialVersionUID = 42L;

    /**
     * 无参构造函数
     */
    public IdleBeatParam() {
    }

    /**
     * 构造函数
     *
     * @param jobId 任务ID
     */
    public IdleBeatParam(int jobId) {
        this.jobId = jobId;
    }

    /**
     * 任务ID
     */
    private int jobId;

}
