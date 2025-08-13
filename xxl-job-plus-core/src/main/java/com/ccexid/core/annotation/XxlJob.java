package com.ccexid.core.annotation;

import java.lang.annotation.*;

/**
 * 方法级别任务处理器注解
 * 用于标记任务执行方法，将方法注册为XXL-Job任务处理器
 *
 * @author xuxueli 2019-12-11 20:50:13
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface XxlJob {

    /**
     * 任务处理器名称
     * 用于在调度中心唯一标识一个任务
     *
     * @return 任务处理器名称
     */
    String value();

    /**
     * 初始化方法名称
     * 在任务线程初始化时调用，用于执行前置初始化操作
     *
     * @return 初始化方法名称，默认为空字符串表示不执行初始化
     */
    String init() default "";

    /**
     * 销毁方法名称
     * 在任务线程销毁时调用，用于执行后置清理操作
     *
     * @return 销毁方法名称，默认为空字符串表示不执行销毁操作
     */
    String destroy() default "";

}
