package com.ccexid.core.biz.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 空闲心跳参数模型类
 * 用于封装空闲心跳检测相关的参数信息
 *
 * @author xuxueli 2020-04-11 22:27
 */
@Data
public class IdleBeatParam implements Serializable {
    private static final long serialVersionUID = 42L;

    /**
     * 任务ID
     * 用于标识具体的心跳检测任务
     */
    private int jobId;
}
