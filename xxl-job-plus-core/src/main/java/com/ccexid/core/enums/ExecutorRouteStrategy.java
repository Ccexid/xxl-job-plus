package com.ccexid.core.enums;

import com.ccexid.core.route.ExecutorRouter;
import com.ccexid.core.route.strategy.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 执行器路由策略枚举类
 * 定义了系统支持的各种执行器路由策略，每种策略对应不同的任务分发方式
 */
@Getter
@AllArgsConstructor
public enum ExecutorRouteStrategy implements IEnums {
    /**
     * 第一个执行器策略 - 总是选择第一个可用的执行器
     */
    FIRST("First", new ExecutorRouteFirst()),

    /**
     * 最后一个执行器策略 - 总是选择最后一个可用的执行器
     */
    LAST("Last", new ExecutorRouteLast()),

    /**
     * 轮询执行器策略 - 按顺序轮流选择执行器
     */
    ROUND("Round", new ExecutorRouteRound()),

    /**
     * 随机执行器策略 - 随机选择一个执行器
     */
    RANDOM("Random", new ExecutorRouteRandom()),

    /**
     * 一致性哈希执行器策略 - 基于一致性哈希算法选择执行器
     */
    CONSISTENT_HASH("Consistent Hash", new ExecutorRouteConsistentHash()),

    /**
     * 最少使用执行器策略 - 选择使用频率最低的执行器
     */
    LEAST_FREQUENTLY_USED("Least Frequently Used", new ExecutorRouteLFU()),

    /**
     * 最近最少使用执行器策略 - 选择最近最少使用的执行器
     */
    LEAST_RECENTLY_USED("Least Recently Used", new ExecutorRouteLRU()),

    /**
     * 故障转移执行器策略 - 支持故障转移的执行器选择
     */
    FAILOVER("Failover", new ExecutorRouteFailover()),

    /**
     * 忙时转移执行器策略 - 当执行器繁忙时进行转移
     */
    BUSY_OVER("Busy over", new ExecutorRouteBusyOver()),

    /**
     * 分片广播执行器策略 - 向所有执行器广播任务
     */
    SHARDING_BROADCAST("Sharding Broadcast", null);

    /**
     * 路由策略的显示标题
     */
    private final String title;

    /**
     * 对应的执行器路由器实现
     */
    private final ExecutorRouter router;
}
