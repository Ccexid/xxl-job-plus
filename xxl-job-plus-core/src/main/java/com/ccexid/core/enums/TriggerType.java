package com.ccexid.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 触发器类型枚举类
 * <p>
 * 定义了系统中支持的各种任务触发器类型，每种类型都有对应的标题描述
 * </p>
 */
@Getter
@AllArgsConstructor
public enum TriggerType implements IEnums {
    /**
     * 手动触发器
     */
    MANUAL("Manual trigger"),

    /**
     * Cron表达式触发器
     */
    CRON("Cron trigger"),

    /**
     * 失败重试触发器
     */
    RETRY("Fail retry trigger"),

    /**
     * 父任务触发器
     */
    PARENT("Parent job trigger"),

    /**
     * API触发器
     */
    API("Api trigger"),

    /**
     * 错过触发补偿触发器
     */
    MISFIRE("Misfire compensation trigger");

    /**
     * 触发器类型的标题描述
     */
    private final String title;
}
