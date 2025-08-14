package com.ccexid.core.thread;


import com.ccexid.core.biz.AdminBiz;
import com.ccexid.core.constant.RegisterConstant;
import com.ccexid.core.context.JobPlusContext;
import com.ccexid.core.context.JobPlusHelper;
import com.ccexid.core.executor.JobExecutor;
import com.ccexid.core.log.JobLogFileAppender;
import com.ccexid.core.model.HandleCallbackParam;
import com.ccexid.core.model.ResponseEntity;
import com.ccexid.core.util.FileUtil;
import com.ccexid.core.util.JdkSerializeTool;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 触发回调线程类，负责处理任务执行结果的回调
 *
 * @author xuxueli 2016-07-22
 */
@Slf4j
public class TriggerCallbackThread implements IThread{

    private static final TriggerCallbackThread INSTANCE = new TriggerCallbackThread();

    public static TriggerCallbackThread getInstance() {
        return INSTANCE;
    }

    /**
     * 任务结果回调队列
     */
    private final LinkedBlockingQueue<HandleCallbackParam> callBackQueue = new LinkedBlockingQueue<>();

    public static void pushCallBack(HandleCallbackParam callback) {
        getInstance().callBackQueue.add(callback);
        log.debug(">>>>>>>>>>> xxl-job, push callback request, logId:{}", callback.getLogId());
    }

    /**
     * 回调线程
     */
    private Thread triggerCallbackThread;
    private Thread triggerRetryCallbackThread;
    private volatile boolean toStop = false;

    public void start() {

        // valid
        if (JobExecutor.getAdminBizList() == null) {
            log.warn(">>>>>>>>>>> xxl-job, executor callback config fail, adminAddresses is null.");
            return;
        }

        // callback
        triggerCallbackThread = new Thread(() -> {

            // normal callback
            while (!toStop) {
                try {
                    HandleCallbackParam callback = getInstance().callBackQueue.take();
                    if (callback != null) {

                        // callback list param
                        List<HandleCallbackParam> callbackParamList = new ArrayList<>();
                        getInstance().callBackQueue.drainTo(callbackParamList);
                        callbackParamList.add(callback);

                        // callback, will retry if error
                        if (!callbackParamList.isEmpty()) {
                            doCallback(callbackParamList);
                        }
                    }
                } catch (Throwable e) {
                    if (!toStop) {
                        log.error(e.getMessage(), e);
                    }
                }
            }

            // last callback
            try {
                List<HandleCallbackParam> callbackParamList = new ArrayList<>();
                getInstance().callBackQueue.drainTo(callbackParamList);
                if (!callbackParamList.isEmpty()) {
                    doCallback(callbackParamList);
                }
            } catch (Throwable e) {
                if (!toStop) {
                    log.error(e.getMessage(), e);
                }
            }
            log.info(">>>>>>>>>>> xxl-job, executor callback thread destroy.");

        });
        triggerCallbackThread.setDaemon(true);
        triggerCallbackThread.setName("xxl-job, executor TriggerCallbackThread");
        triggerCallbackThread.start();


        // retry
        triggerRetryCallbackThread = new Thread(() -> {
            while (!toStop) {
                try {
                    retryFailCallbackFile();
                } catch (Throwable e) {
                    if (!toStop) {
                        log.error(e.getMessage(), e);
                    }

                }
                try {
                    TimeUnit.SECONDS.sleep(RegisterConstant.BEAT_TIMEOUT);
                } catch (InterruptedException e) {
                    if (!toStop) {
                        log.error(e.getMessage(), e);
                    }
                    Thread.currentThread().interrupt(); // 重新设置中断状态
                } catch (Throwable e) {
                    if (!toStop) {
                        log.error(e.getMessage(), e);
                    }
                }
            }
            log.info(">>>>>>>>>>> xxl-job, executor retry callback thread destroy.");
        });
        triggerRetryCallbackThread.setDaemon(true);
        triggerRetryCallbackThread.setName("xxl-job, executor TriggerRetryCallbackThread");
        triggerRetryCallbackThread.start();

    }

    @Override
    public void toStop() {
        toStop = true;
        // stop callback, interrupt and wait
        if (triggerCallbackThread != null) {    // support empty admin address
            triggerCallbackThread.interrupt();
            try {
                triggerCallbackThread.join();
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
                Thread.currentThread().interrupt(); // 重新设置中断状态
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    /**
     * 执行回调，如果出错会重试
     *
     * @param callbackParamList 回调参数列表
     */
    private void doCallback(List<HandleCallbackParam> callbackParamList) {
        boolean callbackRet = false;
        // callback, will retry if error
        for (AdminBiz adminBiz : JobExecutor.getAdminBizList()) {
            try {
                ResponseEntity<String> callbackResult = adminBiz.callback(callbackParamList);
                if (callbackResult != null && ResponseEntity.isSuccess(callbackResult)) {
                    callbackLog(callbackParamList, "<br>----------- xxl-job job callback finish.");
                    callbackRet = true;
                    break;
                } else {
                    callbackLog(callbackParamList, "<br>----------- xxl-job job callback fail, callbackResult:" + callbackResult);
                }
            } catch (Throwable e) {
                callbackLog(callbackParamList, "<br>----------- xxl-job job callback error, errorMsg:" + e.getMessage());
            }
        }
        if (!callbackRet) {
            appendFailCallbackFile(callbackParamList);
        }
    }

    /**
     * 回调日志记录
     *
     * @param callbackParamList 回调参数列表
     * @param logContent        日志内容
     */
    private void callbackLog(List<HandleCallbackParam> callbackParamList, String logContent) {
        for (HandleCallbackParam callbackParam : callbackParamList) {
            String logFileName = JobLogFileAppender.makeLogFileName(new Date(callbackParam.getLogDateTim()), callbackParam.getLogId());
            JobPlusContext.setJobContext(new JobPlusContext(
                    -1,
                    null,
                    logFileName,
                    -1,
                    -1));
            JobPlusHelper.log(logContent);
        }
    }


    // ---------------------- fail-callback file ----------------------

    private static final String FAIL_CALLBACK_FILE_PATH = JobLogFileAppender.getLogPath().concat(File.separator).concat("callback-log").concat(File.separator);
    private static final String FAIL_CALLBACK_FILE_NAME = FAIL_CALLBACK_FILE_PATH.concat("xxl-job-callback-{x}").concat(".log");

    private void appendFailCallbackFile(List<HandleCallbackParam> callbackParamList) {
        // valid
        if (callbackParamList == null || callbackParamList.isEmpty()) {
            return;
        }

        // append file
        byte[] callbackParamListBytes = JdkSerializeTool.serialize(callbackParamList);

        File callbackLogFile = new File(FAIL_CALLBACK_FILE_NAME.replace("{x}", String.valueOf(System.currentTimeMillis())));
        if (callbackLogFile.exists()) {
            for (int i = 0; i < 100; i++) {
                callbackLogFile = new File(FAIL_CALLBACK_FILE_NAME.replace("{x}", String.valueOf(System.currentTimeMillis()).concat("-").concat(String.valueOf(i))));
                if (!callbackLogFile.exists()) {
                    break;
                }
            }
        }
        FileUtil.writeFileContent(callbackLogFile, callbackParamListBytes);
    }

    private void retryFailCallbackFile() {

        // valid
        File callbackLogPath = new File(FAIL_CALLBACK_FILE_PATH);
        if (!callbackLogPath.exists()) {
            return;
        }
        if (callbackLogPath.isFile()) {
            //noinspection ResultOfMethodCallIgnored
            callbackLogPath.delete();
        }
        if (!(callbackLogPath.isDirectory() && callbackLogPath.list() != null && Objects.requireNonNull(callbackLogPath.list()).length > 0)) {
            return;
        }

        // load and clear file, retry
        File[] files = callbackLogPath.listFiles();
        if (files == null) {
            return;
        }

        for (File callbackLogFile : files) {
            byte[] callbackParamListBytes = FileUtil.readFileContent(callbackLogFile);

            // avoid empty file
            if (callbackParamListBytes == null || callbackParamListBytes.length < 1) {
                //noinspection ResultOfMethodCallIgnored
                callbackLogFile.delete();
                continue;
            }

            @SuppressWarnings("unchecked")
            List<HandleCallbackParam> callbackParamList = (List<HandleCallbackParam>) JdkSerializeTool.deserialize(callbackParamListBytes, List.class);

            //noinspection ResultOfMethodCallIgnored
            callbackLogFile.delete();
            doCallback(callbackParamList);
        }

    }

}
