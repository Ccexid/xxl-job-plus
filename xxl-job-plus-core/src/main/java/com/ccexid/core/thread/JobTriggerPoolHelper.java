package com.ccexid.core.thread;

import java.util.concurrent.ThreadPoolExecutor;

public class JobTriggerPoolHelper implements IThread{
    private ThreadPoolExecutor fastTriggerPool = null;
    private ThreadPoolExecutor slowTriggerPool = null;

    public JobTriggerPoolHelper() {
    }

    @Override
    public void start() {

    }

    /**
     * 停止线程
     * 该方法用于通知线程停止执行
     */
    @Override
    public void toStop() {

    }
}
