package com.ccexid.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 调度类型枚举类
 * 用于定义不同的任务调度方式
 */
@Getter
@AllArgsConstructor
public enum ScheduleType {
    /**
     * 无调度类型
     */
    NONE("None"),

    /**
     * Cron表达式调度类型
     */
    CRON("Cron"),

    /**
     * 固定频率调度类型
     */
    FIX_RATE("Fix rate"),
    ;

    /**
     * 调度类型的显示标题
     */
    private final String title;
}
