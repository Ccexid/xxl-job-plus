package com.ccexid.core.context;

import com.ccexid.core.log.XxlJobFileAppender;
import com.ccexid.core.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Optional;

/**
 * XXL-Job任务辅助工具类
 * 提供日志记录、任务结果处理、上下文信息获取等功能
 *
 * @author xuxueli 2020-11-05
 */
public class XxlJobHelper {
    private static final Logger logger = LoggerFactory.getLogger("xxl-job logger");

    // ---------------------- 任务基本信息获取 ----------------------

    /**
     * 获取当前任务ID
     *
     * @return 任务ID，无上下文时返回-1
     */
    public static long getJobId() {
        return getContext()
                .map(XxlJobContext::getJobId)
                .orElse(-1L);
    }

    /**
     * 获取当前任务参数
     *
     * @return 任务参数，无上下文时返回null
     */
    public static String getJobParam() {
        return getContext()
                .map(XxlJobContext::getJobParam)
                .orElse(null);
    }

    // ---------------------- 日志相关信息 ----------------------

    /**
     * 获取当前任务日志文件名
     *
     * @return 日志文件名，无上下文时返回null
     */
    public static String getJobLogFileName() {
        return getContext()
                .map(XxlJobContext::getJobLogFileName)
                .orElse(null);
    }

    // ---------------------- 分片信息获取 ----------------------

    /**
     * 获取当前分片索引
     *
     * @return 分片索引，无上下文时返回-1
     */
    public static int getShardIndex() {
        return getContext()
                .map(XxlJobContext::getShardIndex)
                .orElse(-1);
    }

    /**
     * 获取总分片数
     *
     * @return 总分片数，无上下文时返回-1
     */
    public static int getShardTotal() {
        return getContext()
                .map(XxlJobContext::getShardTotal)
                .orElse(-1);
    }

    // ---------------------- 日志记录工具 ----------------------

    /**
     * 记录格式化日志
     *
     * @param logPattern 日志格式，如"aaa {} bbb {} ccc"
     * @param arguments  格式化参数
     * @return 是否记录成功
     */
    public static boolean log(String logPattern, Object... arguments) {
        FormattingTuple formattedLog = MessageFormatter.arrayFormat(logPattern, arguments);
        StackTraceElement callerInfo = new Throwable().getStackTrace()[1];
        return logDetail(callerInfo, formattedLog.getMessage());
    }

    /**
     * 记录异常堆栈日志
     *
     * @param throwable 异常对象
     * @return 是否记录成功
     */
    public static boolean log(Throwable throwable) {
        StringWriter errorWriter = new StringWriter();
        throwable.printStackTrace(new PrintWriter(errorWriter));
        StackTraceElement callerInfo = new Throwable().getStackTrace()[1];
        return logDetail(callerInfo, errorWriter.toString());
    }

    /**
     * 详细日志记录实现
     */
    private static boolean logDetail(StackTraceElement callerInfo, String logContent) {
        return getContext().map(context -> {
            String formattedLog = buildFormattedLog(callerInfo, logContent);
            String logFileName = context.getJobLogFileName();

            if (isValidLogFileName(logFileName)) {
                XxlJobFileAppender.appendLog(logFileName, formattedLog);
                return true;
            } else {
                logger.info(">>>>>>>>>>> {}", formattedLog);
                return false;
            }
        }).orElse(false);
    }

    /**
     * 构建格式化的日志内容
     */
    private static String buildFormattedLog(StackTraceElement callerInfo, String logContent) {
        return new StringBuilder()
                .append(DateUtils.formatDateTime(new Date()))
                .append(" [")
                .append(callerInfo.getClassName()).append("#").append(callerInfo.getMethodName())
                .append("]-[").append(callerInfo.getLineNumber())
                .append("]-[").append(Thread.currentThread().getName())
                .append("] ")
                .append(logContent != null ? logContent : "")
                .toString();
    }

    /**
     * 验证日志文件名是否有效
     */
    private static boolean isValidLogFileName(String logFileName) {
        return logFileName != null && !logFileName.trim().isEmpty();
    }

    // ---------------------- 任务结果处理工具 ----------------------

    /**
     * 标记任务处理成功
     *
     * @return 是否处理成功
     */
    public static boolean handleSuccess() {
        return handleResult(XxlJobContext.HandleCode.SUCCESS, null);
    }

    /**
     * 标记任务处理成功并添加消息
     *
     * @param message 处理结果消息
     * @return 是否处理成功
     */
    public static boolean handleSuccess(String message) {
        return handleResult(XxlJobContext.HandleCode.SUCCESS, message);
    }

    /**
     * 标记任务处理失败
     *
     * @return 是否处理成功
     */
    public static boolean handleFail() {
        return handleResult(XxlJobContext.HandleCode.FAIL, null);
    }

    /**
     * 标记任务处理失败并添加消息
     *
     * @param message 处理结果消息
     * @return 是否处理成功
     */
    public static boolean handleFail(String message) {
        return handleResult(XxlJobContext.HandleCode.FAIL, message);
    }

    /**
     * 标记任务处理超时
     *
     * @return 是否处理成功
     */
    public static boolean handleTimeout() {
        return handleResult(XxlJobContext.HandleCode.TIMEOUT, null);
    }

    /**
     * 标记任务处理超时并添加消息
     *
     * @param message 处理结果消息
     * @return 是否处理成功
     */
    public static boolean handleTimeout(String message) {
        return handleResult(XxlJobContext.HandleCode.TIMEOUT, message);
    }

    /**
     * 处理任务结果
     *
     * @param handleCode 处理状态码
     * @param message    处理结果消息
     * @return 是否处理成功
     */
    public static boolean handleResult(XxlJobContext.HandleCode handleCode, String message) {
        return getContext().map(context -> {
            context.setHandleCode(handleCode);
            Optional.ofNullable(message).ifPresent(context::setHandleMsg);
            return true;
        }).orElse(false);
    }

    // ---------------------- 内部工具方法 ----------------------

    /**
     * 获取任务上下文（封装空值处理）
     */
    private static Optional<XxlJobContext> getContext() {
        return Optional.ofNullable(XxlJobContext.getContext());
    }
}
