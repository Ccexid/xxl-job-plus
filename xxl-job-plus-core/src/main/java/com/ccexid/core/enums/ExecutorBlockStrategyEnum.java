package com.ccexid.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by xuxueli on 17/5/9.
 */

/**
 * 执行器阻塞策略枚举类
 * 定义了任务执行时的阻塞处理策略
 */
@Getter
@AllArgsConstructor
public enum ExecutorBlockStrategyEnum implements IEnum {

    /**
     * 串行执行策略
     * 当前任务执行完毕后才执行下一个任务
     */
    SERIAL_EXECUTION("Serial execution"),

    /*CONCURRENT_EXECUTION("并行"),*/

    /**
     * 丢弃后续策略
     * 当前任务正在执行时，新到来的任务将被丢弃
     */
    DISCARD_LATER("Discard Later"),

    /**
     * 覆盖早期策略
     * 当前任务正在执行时，新到来的任务将覆盖并终止当前任务
     */
    COVER_EARLY("Cover Early");

    /**
     * 策略标题描述
     */
    private final String title;

}
