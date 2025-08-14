package com.ccexid.core.constant;

/**
 * 注册相关的常量接口
 * 定义了注册过程中使用的时间超时常量
 */
public interface RegisterConstant {
    /**
     * 心跳超时时间（秒）
     * 用于检测注册客户端是否存活的超时时间
     */
    int BEAT_TIMEOUT = 30;

    /**
     * 死亡超时时间（秒）
     * 当超过此时间未收到心跳时，认为注册客户端已死亡
     * 值为心跳超时时间的3倍
     */
    int DEAD_TIMEOUT = BEAT_TIMEOUT * 3;
}

