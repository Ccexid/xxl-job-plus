package com.ccexid.core.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 任务终止参数实体类
 * 用于封装终止任务请求的参数信息，主要包含需要终止的任务ID
 *
 * @author xuxueli 2020-04-11 22:27
 */
@Data
public class KillParam implements Serializable {
    private static final long serialVersionUID = 42L;

    /**
     * 无参构造函数
     */
    public KillParam() {
    }

    /**
     * 构造函数
     *
     * @param jobId 需要终止的任务ID
     */
    public KillParam(int jobId) {
        this.jobId = jobId;
    }

    /**
     * 任务ID
     */
    private int jobId;
}
