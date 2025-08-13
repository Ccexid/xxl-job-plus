package com.ccexid.core.executor;

import com.ccexid.core.handler.IJobHandler;
import com.ccexid.core.handler.annotation.XxlJob;
import com.ccexid.core.log.XxlJobFileAppender;
import com.ccexid.core.server.EmbedServer;
import com.ccexid.core.service.AdminService;
import com.ccexid.core.service.client.AdminBizClient;
import com.ccexid.core.thread.JobLogFileCleanThread;
import com.ccexid.core.thread.JobThread;
import com.ccexid.core.thread.TriggerCallbackThread;
import com.ccexid.core.utils.IpUtils;
import com.ccexid.core.utils.NetUtils;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * XXL-Job执行器核心类
 * 负责初始化执行器组件、管理任务处理器和任务线程、协调与调度中心的通信
 *
 * @author xuxueli 2016/3/2 21:14
 */
@Setter
public class XxlJobExecutor {
    private static final Logger logger = LoggerFactory.getLogger(XxlJobExecutor.class);

    // ---------------------- 配置参数 ----------------------
    private String adminServerAddresses;  // 调度中心地址列表
    private String accessToken;           // 通信令牌
    private int timeout;                  // 通信超时时间(秒)
    private String applicationName;       // 应用名称
    private String serverAddress;         // 执行器地址
    private String ipAddress;             // 绑定IP
    private int port;                     // 绑定端口
    private String logBasePath;           // 日志存储路径
    private int logRetentionDays;         // 日志保留天数

    // ---------------------- 组件实例 ----------------------
    private static List<AdminService> adminClients;  // 调度中心客户端列表
    private EmbedServer embeddedServer;          // 嵌入式服务器
    private static final ConcurrentMap<String, IJobHandler> JOB_HANDLER_REGISTRY = new ConcurrentHashMap<>();  // 任务处理器注册表
    private static final ConcurrentMap<Integer, JobThread> JOB_THREAD_REGISTRY = new ConcurrentHashMap<>();   // 任务线程注册表

    // ---------------------- 生命周期管理 ----------------------

    /**
     * 初始化执行器
     */
    public void start() throws Exception {
        // 1. 初始化日志路径
        XxlJobFileAppender.initLogPath(logBasePath);

        // 2. 初始化调度中心客户端
        initializeAdminClients();

        // 3. 初始化日志清理线程
        JobLogFileCleanThread.getInstance().start(logRetentionDays);

        // 4. 初始化触发回调线程
        TriggerCallbackThread.getInstance().start();

        // 5. 初始化嵌入式服务器
        initializeEmbeddedServer();

        logger.info(">>>>>>>>>>> XXL-Job执行器初始化完成");
    }

    /**
     * 销毁执行器资源
     */
    public void destroy() {
        // 1. 停止嵌入式服务器
        stopEmbeddedServer();

        // 2. 清理任务线程
        clearJobThreads();

        // 3. 清理任务处理器注册表
        JOB_HANDLER_REGISTRY.clear();

        // 4. 停止日志清理线程
        JobLogFileCleanThread.getInstance().stop();

        // 5. 停止触发回调线程
        TriggerCallbackThread.getInstance().stop();

        logger.info(">>>>>>>>>>> XXL-Job执行器资源销毁完成");
    }

    // ---------------------- 调度中心客户端管理 ----------------------

    /**
     * 初始化调度中心客户端列表
     */
    private void initializeAdminClients() throws Exception {
        adminClients = Optional.ofNullable(adminServerAddresses)
                .filter(addresses -> !addresses.trim().isEmpty())
                .map(addresses -> Stream.of(addresses.trim().split(","))
                        .filter(addr -> addr != null && !addr.trim().isEmpty())
                        .map(addr -> (AdminService) new AdminBizClient(addr.trim(), accessToken, timeout))
                        .collect(Collectors.toList()))
                .orElse(new ArrayList<>());
    }

    public static List<AdminService> getAdminClients() {
        return adminClients;
    }

    // ---------------------- 嵌入式服务器管理 ----------------------

    /**
     * 初始化嵌入式服务器
     */
    private void initializeEmbeddedServer() throws Exception {
        // 解析并确定端口（默认从9999开始查找可用端口）
        int bindPort = Optional.of(port).filter(p -> p > 0)
                .orElseGet(() -> NetUtils.findAvailablePort(9999));

        // 解析并确定IP地址（默认取本地IP）
        String bindIp = Optional.ofNullable(ipAddress)
                .filter(ip -> !ip.trim().isEmpty())
                .orElse(IpUtils.getIp());

        // 生成服务器地址（默认格式：http://ip:port/）
        String serverAddr = Optional.ofNullable(serverAddress)
                .filter(addr -> !addr.trim().isEmpty())
                .orElseGet(() -> "http://" + IpUtils.getIpPort(bindIp, bindPort) + "/");

        // 启动嵌入式服务器
        embeddedServer = new EmbedServer();
        embeddedServer.start(serverAddr, bindPort, applicationName, accessToken);

        logger.info(">>>>>>>>>>> 嵌入式服务器启动成功，地址: {}", serverAddr);
    }

    /**
     * 停止嵌入式服务器
     */
    private void stopEmbeddedServer() {
        Optional.ofNullable(embeddedServer).ifPresent(server -> {
            try {
                server.stop();
                logger.info(">>>>>>>>>>> 嵌入式服务器已停止");
            } catch (Exception e) {
                logger.error("停止嵌入式服务器失败", e);
            }
        });
    }

    // ---------------------- 任务处理器管理 ----------------------

    /**
     * 加载任务处理器
     */
    public static IJobHandler loadJobHandler(String handlerName) {
        return JOB_HANDLER_REGISTRY.get(handlerName);
    }

    /**
     * 注册任务处理器
     */
    public static IJobHandler registerJobHandler(String handlerName, IJobHandler jobHandler) {
        Objects.requireNonNull(handlerName, "任务处理器名称不能为空");
        Objects.requireNonNull(jobHandler, "任务处理器实例不能为空");

        IJobHandler existingHandler = JOB_HANDLER_REGISTRY.putIfAbsent(handlerName, jobHandler);
        if (existingHandler != null) {
            logger.warn(">>>>>>>>>>> 任务处理器[{}]已存在，注册失败", handlerName);
            return existingHandler;
        }
        logger.info(">>>>>>>>>>> 任务处理器[{}]注册成功", handlerName);
        return jobHandler;
    }

    /**
     * 注册基于注解的任务处理器
     */
    protected void registerAnnotatedJobHandler(XxlJob xxlJob, Object bean, Method executeMethod) {
        if (xxlJob == null) {
            return;
        }

        String handlerName = xxlJob.value();
        Class<?> beanClass = bean.getClass();
        String methodName = executeMethod.getName();

        // 校验处理器名称
        if (handlerName.trim().isEmpty()) {
            throw new RuntimeException("任务处理器名称无效，bean: " + beanClass + ", 方法: " + methodName);
        }

        // 校验处理器是否已存在
        if (loadJobHandler(handlerName) != null) {
            throw new RuntimeException("任务处理器[" + handlerName + "]命名冲突");
        }

        // 设置执行方法可访问
        executeMethod.setAccessible(true);

        // 解析初始化和销毁方法
        Method initMethod = resolveMethod(beanClass, xxlJob.init(), "初始化方法");
        Method destroyMethod = resolveMethod(beanClass, xxlJob.destroy(), "销毁方法");

        // 注册方法型任务处理器
//        registerJobHandler(handlerName, new MethodJobHandler(bean, executeMethod, initMethod, destroyMethod));
    }

    /**
     * 解析方法（初始化/销毁）
     */
    private Method resolveMethod(Class<?> beanClass, String methodName, String methodType) {
        if (methodName == null || methodName.trim().isEmpty()) {
            return null;
        }
        try {
            Method method = beanClass.getDeclaredMethod(methodName);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("任务处理器" + methodType + "无效，bean: " + beanClass + ", 方法: " + methodName, e);
        }
    }

    // ---------------------- 任务线程管理 ----------------------

    /**
     * 注册任务线程
     */
    public static JobThread registerJobThread(int jobId, IJobHandler handler, String removeOldReason) {
        Objects.requireNonNull(handler, "任务处理器不能为空");

        // 创建新线程并启动
        JobThread newJobThread = new JobThread(jobId, handler);
        newJobThread.start();
        logger.info(">>>>>>>>>>> 任务线程[{}]注册成功", jobId);

        // 替换旧线程（如果存在）
        JobThread oldJobThread = JOB_THREAD_REGISTRY.put(jobId, newJobThread);
        if (oldJobThread != null) {
            oldJobThread.stopThread(removeOldReason);
            oldJobThread.interrupt();
        }

        return newJobThread;
    }

    /**
     * 移除任务线程
     */
    public static JobThread removeJobThread(int jobId, String removeReason) {
        JobThread jobThread = JOB_THREAD_REGISTRY.remove(jobId);
        if (jobThread != null) {
            jobThread.stopThread(removeReason);
            jobThread.interrupt();
            logger.info(">>>>>>>>>>> 任务线程[{}]已移除，原因: {}", jobId, removeReason);
        }
        return jobThread;
    }

    /**
     * 加载任务线程
     */
    public static JobThread loadJobThread(int jobId) {
        return JOB_THREAD_REGISTRY.get(jobId);
    }

    /**
     * 清理所有任务线程
     */
    private void clearJobThreads() {
        if (!JOB_THREAD_REGISTRY.isEmpty()) {
            JOB_THREAD_REGISTRY.forEach((jobId, jobThread) -> {
                JobThread oldJobThread = removeJobThread(jobId, "执行器销毁，终止任务线程");
                if (oldJobThread != null) {
                    try {
                        oldJobThread.join();  // 等待线程执行完毕
                    } catch (InterruptedException e) {
                        logger.error("任务线程[{}]销毁等待失败", jobId, e);
                    }
                }
            });
            JOB_THREAD_REGISTRY.clear();
        }
    }
}