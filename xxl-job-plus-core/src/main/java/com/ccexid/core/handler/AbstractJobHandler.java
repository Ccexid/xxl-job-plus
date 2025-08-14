package com.ccexid.core.handler;

/**
 * 抽象作业处理器类实现了作业处理器接口，用于定义作业处理器的抽象基类。
 * 该类要求所有继承的子类必须实现execute方法来执行具体的作业逻辑。
 *
 * @author ccexid
 * @since 1.0.0
 */
public abstract class AbstractJobHandler implements IJobHandler {

    /**
     * 抽象方法execute，用于执行具体的作业处理逻辑。
     * 子类必须实现此方法来定义作业的具体执行过程。
     *
     * @throws Exception 当作业执行过程中发生错误时抛出异常
     */
    public abstract void execute() throws Exception;
}

