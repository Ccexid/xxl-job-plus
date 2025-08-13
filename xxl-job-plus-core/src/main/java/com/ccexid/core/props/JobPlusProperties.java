package com.ccexid.core.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 任务执行器配置属性类
 * 用于封装XXL-Job任务执行器的各项配置参数
 *
 * @author xuxueli 2018-11-01 09:24:52
 */
@Data
@ConfigurationProperties(prefix = "job.plus")
public class JobPlusProperties {
    /**
     * 默认端口
     * <p>
     * 这里使用 -1 表示随机
     */
    private static final Integer PORT_DEFAULT = -1;

    /**
     * 默认日志保留天数
     * <p>
     * 如果想永久保留，则设置为 -1
     */
    private static final Integer LOG_RETENTION_DAYS_DEFAULT = 30;

    @Data
    private static class AdminProperties {
        /**
         * 调度中心地址列表
         */
        private String addresses;
    }

    @Data
    public static class ExecutorProperties {
        /**
         * 通信超时时间(秒)
         */
        private int timeout;

        /**
         * 应用名称
         * 执行器注册到调度中心时使用
         */
        private String appName;

        /**
         * 执行器地址
         * 调度中心回调时使用
         */
        private String address;

        /**
         * 绑定的IP地址
         * 为空则自动获取
         */
        private String ip;

        /**
         * 绑定的端口号
         * 为0则自动获取
         */
        private int port = PORT_DEFAULT;

        /**
         * 日志存储路径
         * 为空则使用默认路径
         */
        private String logPath;

        /**
         * 日志保留天数
         * 小于3天则不清理
         */
        private int logRetentionDays = LOG_RETENTION_DAYS_DEFAULT;
    }

    /**
     * 访问令牌
     * 通信令牌，用于安全认证
     */
    private String accessToken;
    /**
     * 调度中心配置
     */
    private AdminProperties admin;

    /**
     * 执行器配置
     */
    private ExecutorProperties executor;
}
