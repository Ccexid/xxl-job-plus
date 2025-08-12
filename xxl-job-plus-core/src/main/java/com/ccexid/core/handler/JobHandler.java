package com.ccexid.core.handler;

/**
 * 任务处理器接口，定义任务执行的核心行为契约
 * <p>
 * 所有XXL-Job任务处理器需实现此接口，或继承其抽象实现类{@link IJobHandler}
 *
 * @author xuxueli 2015-12-19 19:06:38
 * @since 1.0.0
 */
public interface JobHandler {

    /**
     * 任务初始化方法
     * <p>
     * 任务线程初始化时调用，用于执行资源加载等初始化操作
     *
     * @throws Exception 初始化过程中发生的异常，会导致任务启动失败
     */
    void init() throws Exception;

    /**
     * 任务销毁方法
     * <p>
     * 任务线程销毁时调用，用于执行资源释放等清理操作
     *
     * @throws Exception 销毁过程中发生的异常，框架会记录但不影响线程终止
     */
    void destroy() throws Exception;
}
