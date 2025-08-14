package com.ccexid.core.executor;

import com.ccexid.core.annotation.XxlJob;
import com.ccexid.core.biz.AdminBiz;
import com.ccexid.core.biz.client.AdminBizClient;
import com.ccexid.core.handler.AbstractJobHandler;
import com.ccexid.core.handler.impl.MethodJobHandler;
import com.ccexid.core.log.JobLogFileAppender;
import com.ccexid.core.props.JobPlusProperties;
import com.ccexid.core.server.EmbedServer;
import com.ccexid.core.thread.JobLogFileCleanThread;
import com.ccexid.core.thread.JobThread;
import com.ccexid.core.thread.TriggerCallbackThread;
import com.ccexid.core.util.IpUtil;
import com.ccexid.core.util.NetUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
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
    private static final ConcurrentMap<String, AbstractJobHandler> JOB_HANDLER_REPOSITORY = new ConcurrentHashMap<>();
    private static final ConcurrentMap<Integer, JobThread> JOB_THREAD_MAP = new ConcurrentHashMap<>();

    private static List<AdminBiz> adminList;
    private EmbedServer embedServer;

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
        // 初始化管理员业务接口列表
        initAdminBizList(jobPlusProperties.getAdmin().getAddresses(), jobPlusProperties.getAccessToken(), jobPlusProperties.getExecutor().getTimeout());
        // 启动日志文件清理线程
        JobLogFileCleanThread.getInstance().start(jobPlusProperties.getExecutor().getLogRetentionDays());
        // 启动触发回调线程
        TriggerCallbackThread.getInstance().start();
        // 初始化内嵌服务器配置
        initEmbedServer(jobPlusProperties.getExecutor().getAddress(),
                jobPlusProperties.getExecutor().getIp(),
                jobPlusProperties.getExecutor().getPort(),
                jobPlusProperties.getExecutor().getAppName(),
                jobPlusProperties.getAccessToken());
    }

    /**
     * 销毁任务执行器，停止所有任务线程
     */
    public void destroy() throws Exception {
        stopEmbedServer();
        // 停止所有任务线程
        stopAllJobThreads();
        // 停止日志文件清理线程
        JobLogFileCleanThread.getInstance().toStop();
        // 停止触发回调线程
        TriggerCallbackThread.getInstance().toStop();
    }

    public static AbstractJobHandler registerJobHandler(String name, AbstractJobHandler jobHandler) {
        log.info(">>>>>>>>>>> xxl-job register job handler success, name:{}, jobHandler:{}", name, jobHandler);
        return JOB_HANDLER_REPOSITORY.put(name, jobHandler);
    }

    public static JobThread registerJobHandler(int jobId, AbstractJobHandler handler, String removeOldReason){
        JobThread newJobThread = new JobThread(jobId, handler);
        newJobThread.start();
        log.info(">>>>>>>>>>> xxl-job regist JobThread success, jobId:{}, handler:{}", new Object[]{jobId, handler});

        JobThread oldJobThread = JOB_THREAD_MAP.put(jobId, newJobThread);	// putIfAbsent | oh my god, map's put method return the old value!!!
        if (oldJobThread != null) {
            oldJobThread.toStop(removeOldReason);
            oldJobThread.interrupt();
        }

        return newJobThread;
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
        if (xxlJob == null) {
            return;
        }

        String name = xxlJob.value();

        Class<?> clazz = bean.getClass();
        String methodName = executeMethod.getName();

        if (StringUtils.isBlank(name)) {
            throw new RuntimeException("xxl-job method-job handler name invalid, for[" + clazz + "#" + methodName + "] .");
        }

        if (loadJobHandler(name) != null) {
            throw new RuntimeException("xxl-job job handler[" + name + "] naming conflicts.");
        }

        executeMethod.setAccessible(true);

        Method initMethod = null;
        Method destroyMethod = null;

        if (StringUtils.isNotBlank(xxlJob.init())) {
            try {
                initMethod = clazz.getDeclaredMethod(xxlJob.init());
                initMethod.setAccessible(true);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("xxl-job method-job handler initMethod invalid, for[" + clazz + "#" + methodName + "] .");
            }
        }

        if (StringUtils.isNotBlank(xxlJob.destroy())) {
            try {
                destroyMethod = clazz.getDeclaredMethod(xxlJob.destroy());
                destroyMethod.setAccessible(true);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("xxl-job method-job handler destroyMethod invalid, for[" + clazz + "#" + methodName + "] .");
            }
        }
        registerJobHandler(name, new MethodJobHandler(bean, executeMethod, initMethod, destroyMethod));
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
            return oldJobThread;
        }
        return null;
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

    private void initAdminBizList(String adminAddresses, String accessToken, int timeout) throws Exception {
        if (StringUtils.isNotBlank(adminAddresses)) {
            if (adminList == null) {
                adminList = new ArrayList<>();
            }
            String[] addressArr = adminAddresses.trim().split(",");
            for (String address : addressArr) {
                if (StringUtils.isNotBlank(address)) {
                    AdminBiz adminBiz = new AdminBizClient(address.trim(), accessToken, timeout);
                    adminList.add(adminBiz);
                }
            }
        }
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
    public static AbstractJobHandler loadJobHandler(String name) {
        return JOB_HANDLER_REPOSITORY.get(name);
    }

    /**
     * 初始化内嵌服务器配置
     *
     * @param address     服务器地址，如果为空则根据ip和端口生成
     * @param ip          服务器IP地址，如果为空则自动获取本机IP
     * @param port        服务器端口，如果小于等于0则自动查找可用端口
     * @param appName     应用名称
     * @param accessToken 访问令牌，用于系统安全验证
     * @throws Exception 初始化过程中可能抛出的异常
     */
    private void initEmbedServer(String address, String ip, int port, String appName, String accessToken) throws Exception {

        // 填充IP和端口信息
        port = port > 0 ? port : NetUtil.findAvailablePort(9999);
        ip = StringUtils.isNotBlank(ip) ? ip : IpUtil.getIp();

        // 生成服务器地址
        if (StringUtils.isBlank(address)) {
            String ipPortAddress = IpUtil.getIpPort(ip, port);   // 注册地址：默认使用address进行注册，如果address为空则使用ip:port
            address = "http://{ip_port}/".replace("{ip_port}", ipPortAddress);
        }

        // 检查访问令牌配置
        if (StringUtils.isBlank(accessToken)) {
            log.warn(">>>>>>>>>>> xxl-job accessToken is empty. To ensure system security, please set the accessToken.");
        }

        // start
        embedServer = new EmbedServer();
        embedServer.start(address, port, appName, accessToken);
    }


    private void stopEmbedServer() {
        // stop provider factory
        if (embedServer != null) {
            try {
                embedServer.stop();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
