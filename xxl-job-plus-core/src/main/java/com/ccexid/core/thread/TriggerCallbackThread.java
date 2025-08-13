package com.ccexid.core.thread;

import com.ccexid.core.constant.RegisterConstant;
import com.ccexid.core.context.XxlJobContext;
import com.ccexid.core.context.XxlJobHelper;
import com.ccexid.core.enums.ResultCode;
import com.ccexid.core.executor.XxlJobExecutor;
import com.ccexid.core.log.XxlJobFileAppender;
import com.ccexid.core.biz.AdminBiz;
import com.ccexid.core.biz.model.ApiResponse;
import com.ccexid.core.biz.model.HandleCallbackParam;
import com.ccexid.core.utils.FileUtils;
import com.ccexid.core.utils.JdkSerializeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 任务触发回调线程
 * 负责处理任务执行结果的回调通知，包括正常回调和失败重试机制
 *
 * @author xuxueli 2016-07-22
 */
public class TriggerCallbackThread {
    private static final Logger logger = LoggerFactory.getLogger(TriggerCallbackThread.class);
    private static final TriggerCallbackThread INSTANCE = new TriggerCallbackThread();

    // 失败回调文件存储相关常量
    private static final String FAIL_CALLBACK_DIR = XxlJobFileAppender.getLogPath()
            + File.separator + "callbacklog" + File.separator;
    private static final String FAIL_CALLBACK_FILE_PATTERN = FAIL_CALLBACK_DIR + "xxl-job-callback-{x}.log";

    // 回调队列
    private final LinkedBlockingQueue<HandleCallbackParam> callbackQueue = new LinkedBlockingQueue<>();

    // 线程变量
    private Thread callbackThread;
    private Thread retryThread;
    private volatile boolean isStopping = false;
    private long retryIntervalSeconds = RegisterConstant.BEAT_TIMEOUT; // 重试间隔时间（秒）
    
    // 回调处理钩子函数，提高可扩展性
    private BiConsumer<List<HandleCallbackParam>, Boolean> callbackResultHook = (params, success) -> {};

    /**
     * 获取单例实例
     */
    public static TriggerCallbackThread getInstance() {
        return INSTANCE;
    }

    /**
     * 向回调队列添加任务结果
     */
    public static void pushCallback(HandleCallbackParam callback) {
        Optional.ofNullable(callback).ifPresent(param -> {
            INSTANCE.callbackQueue.add(param);
            logger.debug(">>>>>>>>>>> xxl-job, 推送回调请求, logId:{}", param.getLogId());
        });
    }

    /**
     * 启动回调线程和重试线程
     */
    public void start() {
        // 验证管理员客户端配置
        if (XxlJobExecutor.getAdminClients() == null) {
            logger.warn(">>>>>>>>>>> xxl-job, 执行器回调配置失败，adminAddresses为空");
            return;
        }

        // 启动正常回调线程
        startCallbackThread();

        // 启动失败重试线程
        startRetryThread();
    }

    /**
     * 停止线程
     */
    public void stop() {
        isStopping = true;

        // 停止回调线程
        stopThread(callbackThread, "回调线程");

        // 停止重试线程
        stopThread(retryThread, "重试线程");
    }

    /**
     * 启动正常回调线程
     */
    private void startCallbackThread() {
        callbackThread = new Thread(() -> {
            // 正常回调循环
            while (!isStopping) {
                try {
                    // 从队列获取回调参数（阻塞等待）
                    HandleCallbackParam callback = callbackQueue.take();
                    Optional.of(callback).ifPresent(this::processCallbackBatch);
                } catch (Throwable e) {
                    if (!isStopping) {
                        logger.error("回调线程异常", e);
                    }
                }
            }

            // 线程停止前处理剩余回调
            processRemainingCallbacks();
            logger.info(">>>>>>>>>>> xxl-job, 执行器回调线程已销毁");
        }, "xxl-job, executor TriggerCallbackThread");

        callbackThread.setDaemon(true);
        callbackThread.start();
    }

    /**
     * 启动失败重试线程
     */
    private void startRetryThread() {
        retryThread = new Thread(() -> {
            while (!isStopping) {
                try {
                    retryFailedCallbacks();
                } catch (Throwable e) {
                    if (!isStopping) {
                        logger.error("重试线程异常", e);
                    }
                }

                // 重试间隔
                try {
                    TimeUnit.SECONDS.sleep(retryIntervalSeconds);
                } catch (InterruptedException e) {
                    if (!isStopping) {
                        logger.error("重试线程等待被中断", e);
                    }
                }
            }
            logger.info(">>>>>>>>>>> xxl-job, 执行器重试回调线程已销毁");
        }, "xxl-job, executor RetryCallbackThread");

        retryThread.setDaemon(true);
        retryThread.start();
    }

    /**
     * 批量处理回调参数
     */
    private void processCallbackBatch(HandleCallbackParam initialCallback) {
        List<HandleCallbackParam> callbackList = new ArrayList<>();
        // 从队列中 draining 所有可用元素
        callbackQueue.drainTo(callbackList);
        callbackList.add(initialCallback);

        if (!callbackList.isEmpty()) {
            processCallbacks(callbackList);
        }
    }

    /**
     * 处理剩余的回调参数（线程停止时）
     */
    private void processRemainingCallbacks() {
        List<HandleCallbackParam> remainingCallbacks = new ArrayList<>();
        callbackQueue.drainTo(remainingCallbacks);

        if (!remainingCallbacks.isEmpty()) {
            processCallbacks(remainingCallbacks);
        }
    }

    /**
     * 执行回调逻辑，失败则写入文件重试
     */
    private void processCallbacks(List<HandleCallbackParam> callbackList) {
        boolean isSuccess = XxlJobExecutor.getAdminClients().stream()
                .anyMatch(adminBiz -> attemptCallback(adminBiz, callbackList));

        // 调用回调结果钩子函数
        callbackResultHook.accept(callbackList, isSuccess);
        
        if (!isSuccess) {
            saveFailedCallbacks(callbackList);
        }
    }

    /**
     * 尝试向单个管理员服务发送回调
     */
    private boolean attemptCallback(AdminBiz adminBiz, List<HandleCallbackParam> callbackList) {
        try {
            ApiResponse<?> result = adminBiz.callback(callbackList);
            if (result != null && ResultCode.SUCCESS.getCode() == result.getCode()) {
                logCallbackResult(callbackList, "<br>----------- xxl-job 任务回调完成");
                return true;
            } else {
                logCallbackResult(callbackList, "<br>----------- xxl-job 任务回调失败, 结果:" + result);
            }
        } catch (Throwable e) {
            logCallbackResult(callbackList, "<br>----------- xxl-job 任务回调异常, 消息:" + e.getMessage());
        }
        return false;
    }

    /**
     * 记录回调日志
     */
    private void logCallbackResult(List<HandleCallbackParam> callbackList, String logContent) {
        callbackList.forEach(param -> {
            String logFileName = XxlJobFileAppender.makeLogFileName(
                    new Date(param.getLogDateTim()), param.getLogId());
            XxlJobContext.setContext(new XxlJobContext(-1, null, logFileName, -1, -1));
            XxlJobHelper.log(logContent);
        });
    }

    /**
     * 保存失败的回调到文件
     */
    private void saveFailedCallbacks(List<HandleCallbackParam> callbackList) {
        if (callbackList.isEmpty()) {
            return;
        }

        // 序列化回调参数
        byte[] data = JdkSerializeUtils.serialize(callbackList);
        if (data == null) {
            return;
        }

        // 创建唯一的失败回调文件
        File callbackFile = createUniqueCallbackFile();
        FileUtils.writeFileContent(callbackFile, data);
    }

    /**
     * 创建唯一的失败回调文件
     */
    private File createUniqueCallbackFile() {
        File baseFile = new File(FAIL_CALLBACK_FILE_PATTERN.replace("{x}", String.valueOf(System.currentTimeMillis())));

        // 如果文件已存在，添加序号后缀
        if (baseFile.exists()) {
            for (int i = 0; i < 100; i++) {
                File numberedFile = new File(FAIL_CALLBACK_FILE_PATTERN
                        .replace("{x}", System.currentTimeMillis() + "-" + i));
                if (!numberedFile.exists()) {
                    return numberedFile;
                }
            }
        }
        return baseFile;
    }

    /**
     * 重试失败的回调
     */
    private void retryFailedCallbacks() {
        File callbackDir = new File(FAIL_CALLBACK_DIR);

        // 验证目录有效性
        if (!isValidCallbackDir(callbackDir)) {
            return;
        }

        // 遍历处理所有失败回调文件
        List<File> callbackFiles = Optional.ofNullable(callbackDir.listFiles())
                .map(Arrays::asList)
                .orElse(new ArrayList<>())
                .stream()
                .filter(File::isFile)
                .collect(Collectors.toList());

        callbackFiles.forEach(this::processCallbackFile);
    }

    /**
     * 验证回调目录是否有效
     */
    private boolean isValidCallbackDir(File dir) {
        if (!dir.exists()) {
            return false;
        }
        if (dir.isFile()) {
            dir.delete();
            return false;
        }
        return dir.list() != null && Objects.requireNonNull(dir.list()).length > 0;
    }

    /**
     * 处理单个回调文件
     */
    private void processCallbackFile(File file) {
        byte[] data = FileUtils.readFileContent(file);

        // 跳过空文件
        if (data == null || data.length < 1) {
            file.delete();
            return;
        }

        // 反序列化并处理回调
        List<HandleCallbackParam> callbackList = (List<HandleCallbackParam>) JdkSerializeUtils.deserialize(data, List.class);
        if (callbackList != null && !callbackList.isEmpty()) {
            file.delete(); // 处理前先删除文件，避免重复处理
            processCallbacks(callbackList);
        }
    }

    /**
     * 停止指定线程
     */
    private void stopThread(Thread thread, String threadName) {
        if (thread != null) {
            thread.interrupt();
            try {
                thread.join();
            } catch (InterruptedException e) {
                logger.error("{}停止失败", threadName, e);
            }
        }
    }
    
    /**
     * 设置重试间隔时间
     * @param retryIntervalSeconds 重试间隔时间（秒）
     */
    public void setRetryIntervalSeconds(long retryIntervalSeconds) {
        this.retryIntervalSeconds = retryIntervalSeconds;
    }
    
    /**
     * 设置回调结果钩子函数
     * @param callbackResultHook 回调结果钩子函数
     */
    public void setCallbackResultHook(BiConsumer<List<HandleCallbackParam>, Boolean> callbackResultHook) {
        this.callbackResultHook = Optional.ofNullable(callbackResultHook).orElse((params, success) -> {});
    }
    
    /**
     * 检查回调线程是否正在运行
     * @return 是否正在运行
     */
    public boolean isCallbackThreadRunning() {
        return callbackThread != null && callbackThread.isAlive() && !isStopping;
    }
    
    /**
     * 检查重试线程是否正在运行
     * @return 是否正在运行
     */
    public boolean isRetryThreadRunning() {
        return retryThread != null && retryThread.isAlive() && !isStopping;
    }
    
    /**
     * 获取队列中待处理的回调数量
     * @return 待处理的回调数量
     */
    public int getPendingCallbackCount() {
        return callbackQueue.size();
    }
}