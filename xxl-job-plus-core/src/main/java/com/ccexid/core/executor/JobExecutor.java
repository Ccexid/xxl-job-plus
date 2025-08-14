package com.ccexid.core.executor;

import com.ccexid.core.annotation.XxlJob;
import com.ccexid.core.biz.AdminBiz;
import com.ccexid.core.handler.IJobHandler;
import com.ccexid.core.log.JobLogFileAppender;
import com.ccexid.core.props.JobPlusProperties;
import com.ccexid.core.thread.JobLogFileCleanThread;
import com.ccexid.core.thread.JobThread;
import com.ccexid.core.thread.TriggerCallbackThread;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 任务执行器类，负责管理任务线程的注册、启动、停止等操作
 *
 * @author xuxueli 2018-11-01 09:24:52
 */
@Slf4j
public class JobExecutor {
    private final JobPlusProperties jobPlusProperties;
    private static final ConcurrentMap<String, IJobHandler> JOB_HANDLER_REPOSITORY = new ConcurrentHashMap<>();
    private static final ConcurrentMap<Integer, JobThread> JOB_THREAD_MAP = new ConcurrentHashMap<>();

    private static List<AdminBiz> adminList;

    /**
     * 构造函数
     *
     * @param jobPlusProperties 任务配置属性对象
     */
    public JobExecutor(JobPlusProperties jobPlusProperties) {
        this.jobPlusProperties = jobPlusProperties;
    }

    /**
     * 获取管理员业务接口列表
     *
     * @return 管理员业务接口列表
     */
    public static List<AdminBiz> getAdminBizList() {
        return adminList;
    }

    /**
     * 启动任务执行器
     *
     * @throws Exception 启动过程中可能抛出的异常
     */
    public void start() throws Exception {
        // 初始化日志路径
        JobLogFileAppender.initLogPath(jobPlusProperties.getExecutor().getLogPath());
        // 启动日志文件清理线程
        JobLogFileCleanThread.getInstance().start(jobPlusProperties.getExecutor().getLogRetentionDays());
        // 启动触发回调线程
        TriggerCallbackThread.getInstance().start();
    }

    /**
     * 销毁任务执行器，停止所有任务线程
     */
    public void destroy() throws Exception {
        stopAllJobThreads();
        // 停止日志文件清理线程
        JobLogFileCleanThread.getInstance().toStop();
        // 停止触发回调线程
        TriggerCallbackThread.getInstance().toStop();
    }

    /**
     * 注册任务处理器
     *
     * @param xxlJob        任务注解对象
     * @param bean          任务处理器bean实例
     * @param executeMethod 任务执行方法
     */
    protected void registerJobHandler(XxlJob xxlJob, Object bean, Method executeMethod) {
        // 注册任务处理器逻辑
    }

    /**
     * 移除指定的任务线程
     *
     * @param jobId           任务ID
     * @param removeOldReason 移除原因说明
     * @return 被移除的任务线程，如果不存在则返回null
     */
    public static JobThread removeJobThread(int jobId, String removeOldReason) {
        JobThread oldJobThread = JOB_THREAD_MAP.remove(jobId);
        if (oldJobThread != null) {
            oldJobThread.toStop(removeOldReason);
            oldJobThread.interrupt();
            log.info(">>>>>>>>>>> xxl-job remove job thread success, jobThread:{}", oldJobThread.getName());
        }
        return oldJobThread;
    }

    /**
     * 停止所有任务线程
     */
    private void stopAllJobThreads() {
        // 遍历所有任务线程并停止
        if (!JOB_THREAD_MAP.isEmpty()) {
            for (Map.Entry<Integer, JobThread> item : JOB_THREAD_MAP.entrySet()) {
                JobThread oldJobThread = removeJobThread(item.getKey(), "web container destroy and kill the job.");
                // wait for job thread push result to callback queue
                if (oldJobThread != null) {
                    try {
                        oldJobThread.join();
                    } catch (InterruptedException e) {
                        log.error(">>>>>>>>>>> xxl-job, JobThread destroy(join) error, jobId:{}", item.getKey(), e);
                        Thread.currentThread().interrupt();
                    }
                }
            }
            // 清空任务线程映射表
            JOB_THREAD_MAP.clear();
        }
        JOB_HANDLER_REPOSITORY.clear();
    }

    /**
     * 加载指定的任务线程
     *
     * @param jobId 任务ID
     * @return 对应的任务线程，如果不存在则返回null
     */
    public static JobThread loadJobThread(int jobId) {
        return JOB_THREAD_MAP.get(jobId);
    }

    /**
     * 根据名称加载作业处理器
     *
     * @param name 作业处理器名称
     * @return 对应名称的作业处理器实例，如果不存在则返回null
     */
    public static IJobHandler loadJobHandler(String name) {
        return JOB_HANDLER_REPOSITORY.get(name);
    }

}
