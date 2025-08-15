package com.ccexid.core.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JobPlusManagerProperties 类用于管理JobPlus相关的配置属性
 * 该类包含了访问令牌、超时时间、邮件发送者等核心配置信息
 */
@Data
@ConfigurationProperties(prefix = "job.plus.manager")
public class JobPlusManagerProperties {

    private static final int TRIGGER_POOL_FAST_MAX = 200;
    private static final int TRIGGER_POOL_SLOW_MAX = 100;
    private static final int LOG_RETENTION_DAYS = 7;

    /**
     * TriggerProperties 类用于管理触发器相关的配置属性
     * 包含快速触发池最大值、慢速触发池最大值和日志保留天数等配置
     */
    @Data
    public static class TriggerProperties {
        private Integer fastMax;
        private Integer slowMax;
        private Integer logRetentionDays;

        /**
         * 获取快速触发池的最大值
         * 如果未设置或超过默认最大值，则返回默认最大值200
         *
         * @return 快速触发池的最大值，范围为0-200
         */
        public Integer getFastMax() {
            if (fastMax == null) {
                return TRIGGER_POOL_FAST_MAX;
            }
            return fastMax > TRIGGER_POOL_FAST_MAX ? TRIGGER_POOL_FAST_MAX : fastMax;
        }

        /**
         * 获取慢速触发池的最大值
         * 如果未设置或超过默认最大值，则返回默认最大值100
         *
         * @return 慢速触发池的最大值，范围为0-100
         */
        public Integer getSlowMax() {
            if (slowMax == null) {
                return TRIGGER_POOL_SLOW_MAX;
            }
            return slowMax > TRIGGER_POOL_SLOW_MAX ? TRIGGER_POOL_SLOW_MAX : slowMax;
        }

        /**
         * 获取日志保留天数
         * 如果未设置，则返回默认值7天
         * 如果设置的值小于默认值，则返回-1表示无限制
         *
         * @return 日志保留天数，-1表示无限制，其他值表示具体天数
         */
        public Integer getLogRetentionDays() {
            if (logRetentionDays == null) {
                return LOG_RETENTION_DAYS;
            }
            return logRetentionDays < LOG_RETENTION_DAYS ? -1 : logRetentionDays;
        }
    }

    private Boolean enable = true;
    /**
     * 访问令牌，用于API认证和授权
     */
    private String accessToken;

    /**
     * 超时时间，单位为秒，用于设置请求或操作的超时限制
     */
    private Integer timeout;

    /**
     * 发件人邮箱地址，用于邮件发送功能中指定发送方
     */
    private String emailFrom;

    /**
     * 触发器属性配置，包含触发器相关的配置参数
     */
    private TriggerProperties trigger;


}