package com.ccexid.core.thread;

import com.ccexid.core.context.JobPlusContext;
import com.ccexid.core.context.JobPlusHelper;
import com.ccexid.core.enums.ResponseCode;
import com.ccexid.core.executor.JobExecutor;
import com.ccexid.core.handler.AbstractJobHandler;
import com.ccexid.core.log.JobLogFileAppender;
import com.ccexid.core.model.HandleCallbackParam;
import com.ccexid.core.model.ResponseEntity;
import com.ccexid.core.model.TriggerParam;
import lombok.extern.slf4j.Slf4j;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 作业线程类，用于执行调度任务
 *
 * @author ccexid
 * @since 1.0.0
 */
@Slf4j
public class JobThread extends Thread {
    private static final int IDLE_LIMIT = 30;
    private static final int HANDLE_MSG_MAX_LENGTH = 50000;
    private static final String HANDLE_MSG_SUFFIX = "...";

    private final int jobId;
    private final AbstractJobHandler handler;
    private final LinkedBlockingQueue<TriggerParam> triggerQueue;
    private final Set<Long> triggerLogIdSet; // avoid repeat trigger for the same TRIGGER_LOG_ID

    private volatile boolean toStop = false;
    private String stopReason;

    private boolean running = false; // if running job
    private int idleTimes = 0;

    /**
     * 构造函数，初始化作业线程
     *
     * @param jobId   作业ID，用于标识不同的作业任务
     * @param handler 作业处理器，负责具体作业逻辑的执行
     */
    public JobThread(int jobId, AbstractJobHandler handler) {
        this.jobId = jobId;
        this.handler = handler;
        this.triggerQueue = new LinkedBlockingQueue<>();
        this.triggerLogIdSet = Collections.synchronizedSet(new HashSet<>());

        // assign job thread name
        this.setName("xxl-job, JobThread-" + jobId + "-" + System.currentTimeMillis());
    }

    /**
     * 获取作业处理器
     *
     * @return 返回当前线程关联的作业处理器
     */
    public AbstractJobHandler getIJobHandler() {
        return handler;
    }

    /**
     * 向触发队列中添加触发参数
     *
     * @param triggerParam 触发参数
     * @return 添加结果响应实体
     */
    public ResponseEntity<String> pushTriggerQueue(TriggerParam triggerParam) {
        // 检查日志ID是否已存在，避免重复触发
        if (triggerLogIdSet.contains(triggerParam.getLogId())) {
            log.info("Duplicate trigger request, logId:{}", triggerParam.getLogId());
            return ResponseEntity.fail();
        }

        triggerLogIdSet.add(triggerParam.getLogId());

        // 尝试将触发参数添加到队列中
        return triggerQueue.add(triggerParam) ? ResponseEntity.success() : ResponseEntity.fail();
    }

    /**
     * 设置停止标志和停止原因
     *
     * @param stopReason 停止原因描述
     */
    public void toStop(String stopReason) {
        this.toStop = true;
        this.stopReason = stopReason;
    }

    public boolean isRunningOrHasQueue() {
        return running || !triggerQueue.isEmpty();
    }

    /**
     * 执行线程的主要逻辑
     * <p>
     * 该方法重写了父类的run方法，用于定义线程的具体执行内容
     * 当线程启动时，会自动调用此方法
     */
    @Override
    public void run() {
        try {
            handler.init();
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }

        while (!toStop) {
            running = false;
            idleTimes++;

            TriggerParam triggerParam = null;
            try {
                triggerParam = triggerQueue.poll(3L, TimeUnit.SECONDS);
                if (triggerParam != null) {
                    running = true;
                    idleTimes = 0;
                    triggerLogIdSet.remove(triggerParam.getLogId());
                    String logFileName = JobLogFileAppender.makeLogFileName(new Date(triggerParam.getLogDateTime()), triggerParam.getLogId());

                    JobPlusContext jobContext = new JobPlusContext(
                            triggerParam.getJobId(),
                            triggerParam.getExecutorParams(),
                            logFileName,
                            triggerParam.getBroadcastIndex(),
                            triggerParam.getBroadcastTotal());
                    JobPlusContext.setJobContext(jobContext);
                    JobPlusHelper.log("<br>----------- xxl-job job execute start -----------<br>----------- Param:" + jobContext.getJobParam());

                    if (triggerParam.getExecutorTimeout() > 0) {
                        Thread futureThread = null;
                        try {
                            Callable<Boolean> task = () -> {
                                // init job context
                                JobPlusContext.setJobContext(jobContext);
                                handler.execute();
                                return true;
                            };
                            FutureTask<Boolean> futureTask = new FutureTask<>(task);
                            futureThread = new Thread(futureTask);
                            futureThread.start();
                            Boolean tempResult = futureTask.get(triggerParam.getExecutorTimeout(), TimeUnit.SECONDS);
                            if (Boolean.TRUE.equals(tempResult)) {
                                JobPlusHelper.log("<br>----------- xxl-job job execute end(success) -----------<br>");
                            } else {
                                JobPlusHelper.log("<br>----------- xxl-job job execute end(fail) -----------<br>");
                            }
                        } catch (TimeoutException e) {
                            JobPlusHelper.log("<br>----------- xxl-job job execute timeout");
                            JobPlusHelper.log(e);
                            JobPlusHelper.handleTimeout("job execute timeout ");
                        } catch (InterruptedException e) {
                            JobPlusHelper.log("<br>----------- xxl-job job execute interrupted");
                            JobPlusHelper.log(e);
                            JobPlusHelper.handleFail("job execute interrupted ");
                            // 恢复中断状态
                            Thread.currentThread().interrupt();
                        } catch (Exception e) {
                            JobPlusHelper.log("<br>----------- xxl-job job execute exception");
                            JobPlusHelper.log(e);
                            JobPlusHelper.handleFail("job execute exception: " + e.getMessage());
                        } finally {
                            if (futureThread != null && futureThread.isAlive()) {
                                futureThread.interrupt();
                            }
                        }
                    } else {
                        try {
                            handler.execute();
                        } catch (Exception e) {
                            JobPlusHelper.log("<br>----------- xxl-job job execute exception");
                            JobPlusHelper.log(e);
                            JobPlusHelper.handleFail("job execute exception: " + e.getMessage());
                        }
                    }

                    if (JobPlusContext.getInstance().getHandleCode() <= 0) {
                        JobPlusHelper.handleFail("job handle result lost.");
                    } else {
                        String tempHandleMsg = JobPlusContext.getInstance().getHandleMsg();
                        if (tempHandleMsg != null && tempHandleMsg.length() > HANDLE_MSG_MAX_LENGTH) {
                            tempHandleMsg = tempHandleMsg.substring(0, HANDLE_MSG_MAX_LENGTH).concat(HANDLE_MSG_SUFFIX);
                        }
                        JobPlusContext.getInstance().setHandleMsg(tempHandleMsg);
                    }

                    JobPlusHelper.log("<br>----------- xxl-job job execute end(finish) -----------<br>----------- Result: handleCode="
                            + JobPlusContext.getInstance().getHandleCode()
                            + ", handleMsg = "
                            + JobPlusContext.getInstance().getHandleMsg()
                    );

                } else {
                    if (idleTimes > IDLE_LIMIT) {
                        if (triggerQueue.isEmpty()) {    // avoid concurrent trigger causes jobId-lost
                            JobExecutor.removeJobThread(jobId, "executor idle times over limit.");
                        }
                    }
                }
            } catch (Throwable e) {
                if (toStop) {
                    JobPlusHelper.log("<br>----------- JobThread toStop, stopReason:" + stopReason);
                }

                StringWriter stringWriter = new StringWriter();
                e.printStackTrace(new PrintWriter(stringWriter));
                String errorMsg = stringWriter.toString();

                JobPlusHelper.handleFail(errorMsg);

                JobPlusHelper.log("<br>----------- JobThread Exception:" + errorMsg + "<br>----------- xxl-job job execute end(error) -----------");
            } finally {
                if (triggerParam != null) {
                    // callback handler info
                    if (!toStop) {
                        // common
                        TriggerCallbackThread.pushCallBack(new HandleCallbackParam(
                                triggerParam.getLogId(),
                                triggerParam.getLogDateTime(),
                                JobPlusContext.getInstance().getHandleCode(),
                                JobPlusContext.getInstance().getHandleMsg())
                        );
                    } else {
                        // is killed
                        TriggerCallbackThread.pushCallBack(new HandleCallbackParam(
                                triggerParam.getLogId(),
                                triggerParam.getLogDateTime(),
                                ResponseCode.FAIL.getCode(),
                                stopReason + " [job running, killed]")
                        );
                    }
                }
            }
        }

        while (triggerQueue != null && !triggerQueue.isEmpty()) {
            TriggerParam triggerParam = triggerQueue.poll();
            if (triggerParam != null) {
                // is killed
                TriggerCallbackThread.pushCallBack(new HandleCallbackParam(
                        triggerParam.getLogId(),
                        triggerParam.getLogDateTime(),
                        ResponseCode.FAIL.getCode(),
                        stopReason + " [job not executed, in the job queue, killed.]")
                );
            }
        }

        // 线程结束时清理资源
        try {
            handler.destroy();
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }

        log.info(">>>>>>>>>>> xxl-job JobThread stopped, hashCode:{}", Thread.currentThread());
    }
}

