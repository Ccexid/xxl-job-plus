package com.ccexid.core.thread;

import com.ccexid.core.context.XxlJobContext;
import com.ccexid.core.context.XxlJobHelper;
import com.ccexid.core.executor.XxlJobExecutor;
import com.ccexid.core.handler.IJobHandler;
import com.ccexid.core.log.XxlJobFileAppender;
import com.ccexid.core.biz.model.ApiResponse;
import com.ccexid.core.biz.model.HandleCallbackParam;
import com.ccexid.core.biz.model.TriggerParam;
import com.ccexid.core.utils.ThrowableUtils;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * 任务执行线程
 * 负责接收任务触发参数、执行任务逻辑、处理超时与异常、回调执行结果
 *
 * @author xuxueli 2016-1-16 19:52:47
 */
public class JobThread extends Thread {
    // 常量定义（提取为静态final，便于集中管理）
    private static final Logger logger = LoggerFactory.getLogger(JobThread.class);
    private static final int IDLE_REMOVE_THRESHOLD = 30;
    private static final long QUEUE_POLL_TIMEOUT = 3L;
    private static final String LOG_START_PREFIX = "<br>----------- XXL-Job任务执行开始 -----------<br>----------- 参数:";
    private static final String LOG_SUCCESS_PREFIX = "<br>----------- XXL-Job任务执行结束(成功) -----------<br>----------- 结果: handleCode=";
    private static final String LOG_EXCEPTION_PREFIX = "<br>----------- 任务线程异常:";
    private static final String LOG_EXCEPTION_SUFFIX = "<br>----------- 任务执行结束(异常) -----------";
    private static final int MAX_HANDLE_MSG_LENGTH = 50000;

    private final int jobId;
    @Getter
    private final IJobHandler jobHandler;
    private final BlockingQueue<TriggerParam> triggerQueue;
    private final Set<Long> triggerLogIds;  // 简化命名，明确存储日志ID

    private volatile boolean isStopping = false;
    private String stopReason;
    private volatile boolean isRunning = false;
    private final AtomicInteger idleCount = new AtomicInteger(0);
    
    // 任务执行前后钩子函数，提高可扩展性
    private Consumer<XxlJobContext> beforeExecuteHook = context -> {};
    private BiConsumer<XxlJobContext, Throwable> afterExecuteHook = (context, throwable) -> {};

    // 构造函数（依赖注入，便于测试时替换）
    public JobThread(int jobId, IJobHandler jobHandler) {
        this.jobId = jobId;
        this.jobHandler = jobHandler;
        this.triggerQueue = new LinkedBlockingQueue<>();
        this.triggerLogIds = ConcurrentHashMap.newKeySet();

        this.setName("xxl-job, JobThread-" + jobId + "-" + System.currentTimeMillis());
    }

    /**
     * 向队列添加触发参数
     */
    public ApiResponse<Void> addTrigger(TriggerParam triggerParam) {
        return Optional.ofNullable(triggerParam)
                .filter(param -> !triggerLogIds.contains(param.getLogId()))
                .map(param -> {
                    triggerLogIds.add(param.getLogId());
                    triggerQueue.add(param);
                    return ApiResponse.SUCCESS;
                })
                .orElseGet(() -> {
                    long logId = Optional.ofNullable(triggerParam).map(TriggerParam::getLogId).orElse(-1L);
                    logger.info(">>>>>>>>>>> 重复触发任务，logId:{}", logId);
                    return ApiResponse.fail("重复触发任务，logId:" + logId);
                });
    }

    /**
     * 停止任务线程
     */
    public void stopThread(String stopReason) {
        this.isStopping = true;
        this.stopReason = Optional.ofNullable(stopReason).orElse("未知原因");
    }

    /**
     * 判断线程是否正在运行或有等待任务
     */
    public boolean hasRunningTaskOrQueue() {
        return isRunning || !triggerQueue.isEmpty();
    }

    @Override
    public void run() {
        try {
            initJobHandler();
            executeTaskLoop();
        } finally {
            handleRemainingTriggers();
            destroyJobHandler();
            logger.info(">>>>>>>>>>> XXL-Job任务线程已停止，线程 hashCode:{}", Thread.currentThread().hashCode());
        }
    }

    /**
     * 初始化任务处理器
     */
    private void initJobHandler() {
        try {
            jobHandler.init();
        } catch (Throwable e) {
            logger.error("任务处理器初始化失败", e);
        }
    }

    /**
     * 任务执行主循环
     */
    private void executeTaskLoop() {
        while (!isStopping) {
            isRunning = false;
            idleCount.incrementAndGet();

            // 使用Optional封装可能为null的结果
            Optional.ofNullable(pollTriggerParam())
                    .ifPresent(this::processTriggerTask);

            if (!isStopping) {
                handleIdleState();
            }
        }
    }

    /**
     * 从队列获取触发参数
     */
    private TriggerParam pollTriggerParam() {
        try {
            return triggerQueue.poll(QUEUE_POLL_TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 恢复中断状态
            return null;
        }
    }

    /**
     * 处理触发任务
     */
    private void processTriggerTask(TriggerParam triggerParam) {
        isRunning = true;
        idleCount.set(0);
        triggerLogIds.remove(triggerParam.getLogId());

        // 构建任务上下文
        XxlJobContext jobContext = buildJobContext(triggerParam);
        XxlJobContext.setContext(jobContext);

        try {
            logTaskStart(jobContext);
            beforeExecuteHook.accept(jobContext);
            executeJob(triggerParam);
            handleTaskResult();
            logTaskSuccess(jobContext);
        } catch (Throwable e) {
            handleTaskException(e);
        } finally {
            afterExecuteHook.accept(jobContext, null);
            sendCallback(triggerParam);
        }
    }

    /**
     * 构建任务上下文
     */
    private XxlJobContext buildJobContext(TriggerParam triggerParam) {
        String logFileName = XxlJobFileAppender.makeLogFileName(
                new Date(triggerParam.getLogDateTime()),
                triggerParam.getLogId()
        );
        return new XxlJobContext(
                triggerParam.getJobId(),
                triggerParam.getExecutorParams(),
                logFileName,
                triggerParam.getBroadcastIndex(),
                triggerParam.getBroadcastTotal()
        );
    }

    /**
     * 执行任务（根据超时配置选择执行方式）
     */
    private void executeJob(TriggerParam triggerParam) throws Exception {
        if (triggerParam.getExecutorTimeout() > 0) {
            executeWithTimeout(triggerParam);
        } else {
            jobHandler.execute();
        }
    }

    /**
     * 带超时控制的任务执行
     */
    private void executeWithTimeout(TriggerParam triggerParam) throws Exception {
        CompletableFuture.runAsync(() -> {
            XxlJobContext.setContext(XxlJobContext.getContext());
            try {
                jobHandler.execute();
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, Executors.newSingleThreadExecutor())
        .get(triggerParam.getExecutorTimeout(), TimeUnit.SECONDS);
    }

    /**
     * 处理空闲状态
     */
    private void handleIdleState() {
        if (idleCount.get() > IDLE_REMOVE_THRESHOLD && triggerQueue.isEmpty()) {
            XxlJobExecutor.removeJobThread(jobId, "执行器空闲次数超过阈值");
        }
    }

    /**
     * 处理任务执行结果
     */
    private void handleTaskResult() {
        XxlJobContext context = XxlJobContext.getContext();
        if (context.getHandleCode().getCode() <= 0) {
            XxlJobHelper.handleFail("任务处理结果丢失");
        } else {
            // 使用Optional处理可能为null的消息
            context.setHandleMsg(Optional.ofNullable(context.getHandleMsg())
                    .filter(msg -> msg.length() <= MAX_HANDLE_MSG_LENGTH)
                    .orElseGet(() -> context.getHandleMsg().substring(0, MAX_HANDLE_MSG_LENGTH) + "..."));
        }
    }

    /**
     * 处理任务执行异常
     */
    private void handleTaskException(Throwable e) {
        if (isStopping) {
            XxlJobHelper.log("<br>----------- 任务线程停止，原因:" + stopReason);
        }

        // 异常信息转换为字符串
        String errorMsg = getStackTraceAsString(e);
        XxlJobHelper.handleFail(errorMsg);
        XxlJobHelper.log(LOG_EXCEPTION_PREFIX + errorMsg + LOG_EXCEPTION_SUFFIX);
    }

    /**
     * 异常堆栈转换为字符串
     */
    private String getStackTraceAsString(Throwable e) {
        return ThrowableUtils.toString(e);
    }

    /**
     * 发送执行结果回调
     */
    private void sendCallback(TriggerParam triggerParam) {
        HandleCallbackParam callbackParam = isStopping
                ? buildStoppedCallback(triggerParam)
                : buildNormalCallback(triggerParam);

        TriggerCallbackThread.pushCallback(callbackParam);
    }

    /**
     * 构建正常执行的回调参数
     */
    private HandleCallbackParam buildNormalCallback(TriggerParam triggerParam) {
        XxlJobContext context = XxlJobContext.getContext();
        return new HandleCallbackParam(
                triggerParam.getLogId(),
                triggerParam.getLogDateTime(),
                context.getHandleCode().getCode(),
                context.getHandleMsg()
        );
    }

    /**
     * 构建已停止状态的回调参数
     */
    private HandleCallbackParam buildStoppedCallback(TriggerParam triggerParam) {
        return new HandleCallbackParam(
                triggerParam.getLogId(),
                triggerParam.getLogDateTime(),
                XxlJobContext.HandleCode.FAIL.getCode(),
                stopReason + " [任务运行中，已终止]"
        );
    }

    /**
     * 处理队列中剩余的触发任务
     */
    private void handleRemainingTriggers() {
        triggerQueue.forEach(triggerParam -> {
            HandleCallbackParam callbackParam = new HandleCallbackParam(
                    triggerParam.getLogId(),
                    triggerParam.getLogDateTime(),
                    XxlJobContext.HandleCode.FAIL.getCode(),
                    stopReason + " [任务未执行，在队列中已终止]"
            );
            TriggerCallbackThread.pushCallback(callbackParam);
        });
        triggerQueue.clear(); // 清空队列释放资源
    }

    /**
     * 销毁任务处理器
     */
    private void destroyJobHandler() {
        try {
            jobHandler.destroy();
        } catch (Throwable e) {
            logger.error("任务处理器销毁失败", e);
        }
    }

    /**
     * 记录任务开始日志
     */
    private void logTaskStart(XxlJobContext context) {
        XxlJobHelper.log(LOG_START_PREFIX + context.getJobParam());
    }

    /**
     * 记录任务成功日志
     */
    private void logTaskSuccess(XxlJobContext context) {
        XxlJobHelper.log(LOG_SUCCESS_PREFIX + context.getHandleCode() + ", handleMsg=" + context.getHandleMsg());
    }
    
    /**
     * 设置任务执行前钩子函数
     * @param beforeExecuteHook 任务执行前钩子函数
     */
    public void setBeforeExecuteHook(Consumer<XxlJobContext> beforeExecuteHook) {
        this.beforeExecuteHook = Optional.ofNullable(beforeExecuteHook).orElse(context -> {});
    }
    
    /**
     * 设置任务执行后钩子函数
     * @param afterExecuteHook 任务执行后钩子函数
     */
    public void setAfterExecuteHook(BiConsumer<XxlJobContext, Throwable> afterExecuteHook) {
        this.afterExecuteHook = Optional.ofNullable(afterExecuteHook).orElse((context, throwable) -> {});
    }
    
    /**
     * 获取任务ID
     * @return 任务ID
     */
    public int getJobId() {
        return jobId;
    }
    
    /**
     * 检查线程是否正在运行
     * @return 是否正在运行
     */
    public boolean isRunning() {
        return isRunning;
    }
    
    /**
     * 获取空闲计数
     * @return 空闲计数
     */
    public int getIdleCount() {
        return idleCount.get();
    }
}