package com.ccexid.core.handler.annotation;

import java.lang.annotation.*;

/**
 * XXL-Job任务处理器注解
 * <p>
 * 用于标记方法作为XXL-Job的任务处理器，通过注解属性配置任务的基本信息
 * 被标注的方法将作为调度任务的执行入口，由XXL-Job框架自动识别和调用
 *
 * @author xuxueli 2019-12-11 20:50:13
 * @see com.ccexid.core.handler.IJobHandler
 * @since 1.0.0
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface XxlJob {

    /**
     * 任务处理器名称
     * <p>
     * 用于唯一标识当前任务处理器，在XXL-Job调度中心配置任务时需要匹配此名称
     * 名称需保证全局唯一，建议采用"业务模块-功能名称"的命名规范
     *
     * @return 任务处理器的唯一名称
     */
    String value();

    /**
     * 初始化方法名称
     * <p>
     * 指定任务线程初始化时需要调用的方法，用于执行初始化逻辑（如资源加载、连接建立等）
     * 方法需为当前类中的无参方法，若未指定则不执行任何初始化操作
     *
     * @return 初始化方法的名称，默认为空字符串（不执行初始化）
     */
    String init() default "";

    /**
     * 销毁方法名称
     * <p>
     * 指定任务线程销毁时需要调用的方法，用于执行资源清理逻辑（如连接关闭、缓存释放等）
     * 方法需为当前类中的无参方法，若未指定则不执行任何销毁操作
     *
     * @return 销毁方法的名称，默认为空字符串（不执行销毁）
     */
    String destroy() default "";

}
