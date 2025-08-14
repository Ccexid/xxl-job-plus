package com.ccexid.core.biz.impl;


import com.ccexid.core.biz.ExecutorBiz;
import com.ccexid.core.enums.ExecutorBlockStrategy;
import com.ccexid.core.enums.GlueType;
import com.ccexid.core.enums.IEnums;
import com.ccexid.core.enums.ResponseCode;
import com.ccexid.core.executor.JobExecutor;
import com.ccexid.core.glue.GlueFactory;
import com.ccexid.core.handler.AbstractJobHandler;
import com.ccexid.core.handler.impl.GlueJobHandler;
import com.ccexid.core.handler.impl.ScriptJobHandler;
import com.ccexid.core.log.JobLogFileAppender;
import com.ccexid.core.model.*;
import com.ccexid.core.thread.JobThread;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * 执行器业务实现类
 *
 * @author xuxueli 2017-03-01
 */
@Slf4j
public class ExecutorBizImpl implements ExecutorBiz {

    @Override
    public ResponseEntity<String> beat() {
        return ResponseEntity.success();
    }

    /**
     * 检查任务是否空闲可用
     *
     * @param idleBeatParam 空闲检查参数，包含任务ID等信息
     * @return ResponseEntity<String> 返回执行结果，如果任务正在运行或有待执行队列则返回失败，否则返回成功
     */
    @Override
    public ResponseEntity<String> idleBeat(IdleBeatParam idleBeatParam) {

        // 检查任务线程是否正在运行或有待执行的队列
        JobThread jobThread = JobExecutor.loadJobThread(idleBeatParam.getJobId());
        if (jobThread != null && jobThread.isRunningOrHasQueue()) {
            return ResponseEntity.fail();
        }
        return ResponseEntity.success();
    }


    @Override
    public ResponseEntity<String> run(TriggerParam triggerParam) {
        // load old：jobHandler + jobThread
        JobThread jobThread = JobExecutor.loadJobThread(triggerParam.getJobId());
        AbstractJobHandler jobHandler = jobThread != null ? jobThread.getIJobHandler() : null;
        String removeOldReason = null;

        // valid：jobHandler + jobThread
        GlueType glueTypeEnum = IEnums.match(GlueType.class, triggerParam.getGlueType(), null);
        if (GlueType.BEAN == glueTypeEnum) {

            // new jobhandler
            AbstractJobHandler newJobHandler = JobExecutor.loadJobHandler(triggerParam.getExecutorHandler());

            // valid old jobThread
            if (jobThread != null && jobHandler != newJobHandler) {
                // change handler, need kill old thread
                removeOldReason = "change job handler or glue type, and terminate the old job thread.";

                jobThread = null;
                jobHandler = null;
            }

            // valid handler
            if (jobHandler == null) {
                jobHandler = newJobHandler;
                if (jobHandler == null) {
                    return ResponseEntity.of(ResponseCode.NOT_FOUND.getCode(), "job handler [" + triggerParam.getExecutorHandler() + "] not found.", null);
                }
            }

        } else if (GlueType.GLUE_GROOVY == glueTypeEnum) {

            // valid old jobThread
            if (jobThread != null &&
                    !(jobThread.getIJobHandler() instanceof GlueJobHandler
                            && ((GlueJobHandler) jobThread.getIJobHandler()).getCurrentGlueUpdateTime() == triggerParam.getGlueUpdateTime())) {
                // change handler or gluesource updated, need kill old thread
                removeOldReason = "change job source or glue type, and terminate the old job thread.";

                jobThread = null;
                jobHandler = null;
            }

            // valid handler
            if (jobHandler == null) {
                try {
                    AbstractJobHandler originJobHandler = GlueFactory.getInstance().loadNewInstance(triggerParam.getGlueSource());
                    jobHandler = new GlueJobHandler(originJobHandler, triggerParam.getGlueUpdateTime());
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    return ResponseEntity.fail();
                }
            }
        } else if (glueTypeEnum != null && glueTypeEnum.isScript()) {

            // valid old jobThread
            if (jobThread != null &&
                    !(jobThread.getIJobHandler() instanceof ScriptJobHandler
                            && ((ScriptJobHandler) jobThread.getIJobHandler()).getCurrentGlueUpdateTime() == triggerParam.getGlueUpdateTime())) {
                // change script or gluesource updated, need kill old thread
                removeOldReason = "change job source or glue type, and terminate the old job thread.";

                jobThread = null;
                jobHandler = null;
            }

            // valid handler
            if (jobHandler == null) {
                jobHandler = new ScriptJobHandler(triggerParam.getJobId(), triggerParam.getGlueUpdateTime(), triggerParam.getGlueSource(), IEnums.match(GlueType.class, triggerParam.getGlueType(), null));
            }
        } else {
            return ResponseEntity.of(ResponseCode.INTERNAL_SERVER_ERROR, "glueType[" + triggerParam.getGlueType() + "] is not valid.");
        }

        // executor block strategy
        if (jobThread != null) {
            ExecutorBlockStrategy blockStrategy = IEnums.match(ExecutorBlockStrategy.class, triggerParam.getExecutorBlockStrategy(), null);
            if (ExecutorBlockStrategy.DISCARD_LATER == blockStrategy) {
                // discard when running
                if (jobThread.isRunningOrHasQueue()) {
                    return ResponseEntity.of(ResponseCode.INTERNAL_SERVER_ERROR, "block strategy effect：" + ExecutorBlockStrategy.DISCARD_LATER.getTitle());
                }
            } else if (ExecutorBlockStrategy.COVER_EARLY == blockStrategy) {
                // kill running jobThread
                if (jobThread.isRunningOrHasQueue()) {
                    removeOldReason = "block strategy effect：" + ExecutorBlockStrategy.COVER_EARLY.getTitle();

                    jobThread = null;
                }
            }
        }

        // replace thread (new or exists invalid)
        if (jobThread == null) {
            jobThread = JobExecutor.registerJobHandler(triggerParam.getJobId(), jobHandler, removeOldReason);
        }

        // push data to queue
        return jobThread.pushTriggerQueue(triggerParam);
    }

    @Override
    public ResponseEntity<String> kill(KillParam killParam) {
        // kill handlerThread, and create new one
        JobThread jobThread = JobExecutor.loadJobThread(killParam.getJobId());
        if (jobThread != null) {
            JobExecutor.removeJobThread(killParam.getJobId(), "scheduling center kill job.");
            return ResponseEntity.success();
        }

        return ResponseEntity.fail();
    }

    @Override
    public ResponseEntity<LogResult> log(LogParam logParam) {
        // log filename: logPath/yyyy-MM-dd/9999.log
        String logFileName = JobLogFileAppender.makeLogFileName(new Date(logParam.getLogDateTim()), logParam.getLogId());

        LogResult logResult = JobLogFileAppender.readLog(logFileName, logParam.getFromLineNum());
        return ResponseEntity.success(logResult);
    }

}
