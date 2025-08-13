package com.ccexid.core.context;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * XXL-Job任务执行上下文
 * 存储任务执行过程中的关键信息，包括任务ID、参数、分片信息及执行结果
 *
 * @author xuxueli 2020-05-21
 */

@Getter
public class XxlJobContext {

    // 处理结果状态码（使用枚举替代常量，增强类型安全）
    public enum HandleCode {
        SUCCESS(200),
        FAIL(500),
        TIMEOUT(502);

        private final int code;

        HandleCode(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        // 根据状态码获取枚举实例
        public static HandleCode of(int code) {
            for (HandleCode handleCode : values()) {
                if (handleCode.code == code) {
                    return handleCode;
                }
            }
            throw new IllegalArgumentException("无效的处理状态码: " + code);
        }
    }

    // ---------------------- getter方法 ----------------------
    // ---------------------- 基础信息 ----------------------
    private final long jobId;                 // 任务ID
    private final String jobParam;            // 任务参数
    private final String jobLogFileName;      // 任务日志文件名

    // ---------------------- 分片信息 ----------------------
    private final int shardIndex;             // 分片索引
    private final int shardTotal;             // 总分片数

    // ---------------------- 处理结果 ----------------------
    private HandleCode handleCode;            // 处理状态码
    @Setter
    private String handleMsg;                 // 处理结果信息

    // 线程上下文持有器（使用泛型简化声明）
    private static final InheritableThreadLocal<XxlJobContext> CONTEXT_HOLDER = new InheritableThreadLocal<>();

    /**
     * 构造函数
     *
     * @param jobId           任务ID
     * @param jobParam        任务参数
     * @param jobLogFileName  日志文件名
     * @param shardIndex      分片索引
     * @param shardTotal      总分片数
     */
    public XxlJobContext(long jobId, String jobParam, String jobLogFileName, int shardIndex, int shardTotal) {
        this.jobId = jobId;
        this.jobParam = Objects.requireNonNull(jobParam, "");  // 避免null值
        this.jobLogFileName = Objects.requireNonNull(jobLogFileName, "日志文件名不能为空");
        this.shardIndex = shardIndex;
        this.shardTotal = shardTotal;
        this.handleCode = HandleCode.SUCCESS;  // 默认成功状态
    }

    public void setHandleCode(HandleCode handleCode) {
        this.handleCode = Objects.requireNonNull(handleCode, "处理状态码不能为空");
    }

    // 兼容旧的int类型状态码设置
    public void setHandleCode(int handleCode) {
        this.handleCode = HandleCode.of(handleCode);
    }

    // ---------------------- 线程上下文工具方法 ----------------------

    /**
     * 设置当前线程的任务上下文
     *
     * @param context 任务上下文对象，null表示清除
     */
    public static void setContext(XxlJobContext context) {
        if (context == null) {
            CONTEXT_HOLDER.remove();
        } else {
            CONTEXT_HOLDER.set(context);
        }
    }

    /**
     * 获取当前线程的任务上下文
     *
     * @return 任务上下文对象
     */
    public static XxlJobContext getContext() {
        return CONTEXT_HOLDER.get();
    }

    /**
     * 清除当前线程的任务上下文
     */
    public static void clearContext() {
        CONTEXT_HOLDER.remove();
    }

    // ---------------------- 重写Object方法 ----------------------

    @Override
    public String toString() {
        return "XxlJobContext{" +
                "jobId=" + jobId +
                ", jobParam='" + jobParam + '\'' +
                ", jobLogFileName='" + jobLogFileName + '\'' +
                ", shardIndex=" + shardIndex +
                ", shardTotal=" + shardTotal +
                ", handleCode=" + handleCode +
                ", handleMsg='" + handleMsg + '\'' +
                '}';
    }
}