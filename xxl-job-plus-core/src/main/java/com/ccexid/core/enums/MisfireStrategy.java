package com.ccexid.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * MisfireStrategy枚举类
 *
 * 该枚举定义了任务调度中错过触发时间时的处理策略
 *
 * @author
 * @since
 */
@AllArgsConstructor
@Getter
public enum MisfireStrategy implements IEnums {
    /**
     * do nothing
     */
    DO_NOTHING("Do nothing"),

    /**
     * fire once now
     */
    FIRE_ONCE_NOW("Fire once now");;

    /**
     * 策略标题描述
     */
    private final String title;
}

