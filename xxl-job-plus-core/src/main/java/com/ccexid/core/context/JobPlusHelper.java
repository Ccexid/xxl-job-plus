package com.ccexid.core.context;

import com.ccexid.core.enums.ResponseCode;
import com.ccexid.core.log.JobLogFileAppender;
import com.ccexid.core.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Optional;

/**
 * JobPlusHelper类提供了对JobPlusContext的便捷访问方法
 * 该类封装了获取作业上下文信息和设置处理结果的静态方法
 */
@Slf4j
public class JobPlusHelper {

    /**
     * 获取当前线程的作业上下文实例
     *
     * @return 返回包含JobPlusContext的Optional对象，如果上下文不存在则返回空Optional
     */
    private static Optional<JobPlusContext> getContext() {
        return Optional.ofNullable(JobPlusContext.getInstance());
    }

    /**
     * 获取当前作业的ID
     *
     * @return 返回作业ID，如果上下文不存在则返回-1
     */
    public static long getJobId() {
        return getContext().map(JobPlusContext::getJobId).orElse(-1L);
    }

    /**
     * 获取当前作业的参数
     *
     * @return 返回作业参数字符串，如果上下文不存在则返回null
     */
    public static String getJobParam() {
        return getContext().map(JobPlusContext::getJobParam).orElse(null);
    }

    /**
     * 获取当前作业的日志文件名
     *
     * @return 返回作业日志文件名，如果上下文不存在则返回null
     */
    public static String getJobLogFileName() {
        return getContext().map(JobPlusContext::getJobLogFileName).orElse(null);
    }

    /**
     * 获取当前分片的索引
     *
     * @return 返回分片索引，如果上下文不存在则返回-1
     */
    public static int getShardIndex() {
        return getContext().map(JobPlusContext::getShardIndex).orElse(-1);
    }

    /**
     * 获取分片总数
     *
     * @return 返回分片总数，如果上下文不存在则返回-1
     */
    public static int getShardTotal() {
        return getContext().map(JobPlusContext::getShardTotal).orElse(-1);
    }

    /**
     * 处理结果并设置到上下文环境中
     *
     * @param handleCode 处理结果代码
     * @param handleMsg  处理结果消息
     * @return 如果上下文存在并成功设置返回true，否则返回false
     */
    public static boolean handleResult(int handleCode, String handleMsg) {
        // 尝试获取上下文，如果存在则设置处理结果代码和消息
        return getContext().map(context -> {
            context.setHandleCode(handleCode);
            context.setHandleMsg(handleMsg);
            return true;
        }).orElse(false);
    }

    /**
     * 设置处理结果为超时状态
     *
     * @param handleMsg 超时消息内容
     * @return 如果上下文存在并成功设置返回true，否则返回false
     */
    public static boolean handleTimeout(String handleMsg) {
        return handleResult(ResponseCode.TIMEOUT.getCode(), handleMsg);
    }

    /**
     * 设置处理结果为失败状态
     *
     * @param handleMsg 失败消息内容
     * @return 如果上下文存在并成功设置返回true，否则返回false
     */
    public static boolean handleFail(String handleMsg) {
        return handleResult(ResponseCode.FAIL.getCode(), handleMsg);
    }

    /**
     * 设置处理结果为成功状态
     *
     * @param handleMsg 成功消息内容
     * @return 如果上下文存在并成功设置返回true，否则返回false
     */
    public static boolean handleSuccess(String handleMsg) {
        return handleResult(ResponseCode.SUCCESS.getCode(), handleMsg);
    }

    /**
     * 设置处理结果为超时状态，使用默认超时消息
     *
     * @return 如果上下文存在并成功设置返回true，否则返回false
     */
    public static boolean handleTimeout() {
        return handleTimeout(ResponseCode.TIMEOUT.getMessage());
    }

    /**
     * 设置处理结果为失败状态，使用默认失败消息
     *
     * @return 如果上下文存在并成功设置返回true，否则返回false
     */
    public static boolean handleFail() {
        return handleFail(ResponseCode.FAIL.getMessage());
    }

    /**
     * 设置处理结果为成功状态，使用默认成功消息
     *
     * @return 如果上下文存在并成功设置返回true，否则返回false
     */
    public static boolean handleSuccess() {
        return handleSuccess(ResponseCode.SUCCESS.getMessage());
    }

    /**
     * 记录详细日志信息
     *
     * @param callInfo  调用堆栈信息，包含类名、方法名、行号等信息
     * @param appendLog 要追加的日志内容
     * @return 日志记录成功返回true，失败返回false
     */
    private static boolean logDetail(StackTraceElement callInfo, String appendLog) {
        if (callInfo == null) {
            log.warn("callInfo is null, skip logging.");
            return false;
        }

        return getContext().map(context -> {
            // 构建日志内容
            StringBuffer sb = new StringBuffer();
            sb.append(DateUtil.formatDateTime(new Date())).append(" ")
                    .append("[").append(callInfo.getClassName()).append("#").append(callInfo.getMethodName()).append("]").append("-")
                    .append("[").append(callInfo.getLineNumber()).append("]").append("-")
                    .append("[").append(Thread.currentThread().getName()).append("] ").append(StringUtils.defaultString(appendLog));

            String formatAppendLog = sb.toString();
            String logFileName = context.getJobLogFileName();

            // 根据日志文件名是否存在决定日志输出方式
            if (StringUtils.isNotBlank(logFileName)) {
                JobLogFileAppender.appendLog(logFileName, formatAppendLog);
                return true;
            } else {
                log.error(formatAppendLog);
                return false;
            }
        }).orElse(false);
    }


    /**
     * 记录异常信息到日志中
     *
     * @param e 需要记录的异常对象
     * @return 记录结果，true表示记录成功，false表示记录失败
     */
    public static boolean log(Throwable e) {

        // 将异常堆栈信息转换为字符串
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        String appendLog = stringWriter.toString();

        // 获取调用此方法的代码位置信息
        StackTraceElement callInfo = new Throwable().getStackTrace()[1];
        return logDetail(callInfo, appendLog);
    }

    /**
     * 记录日志信息
     *
     * @param appendLogPattern   日志格式化模式字符串，使用占位符{}来表示参数位置
     * @param appendLogArguments 日志参数数组，用于替换模式字符串中的占位符
     * @return boolean 返回日志记录是否成功的结果
     */
    public static boolean log(String appendLogPattern, Object... appendLogArguments) {

        // 格式化日志消息，将模式字符串和参数合并成完整的日志内容
        FormattingTuple ft = MessageFormatter.arrayFormat(appendLogPattern, appendLogArguments);
        String appendLog = ft.getMessage();

        // 获取调用栈信息，[1]表示调用当前方法的上一层方法信息
        StackTraceElement callInfo = new Throwable().getStackTrace()[1];
        return logDetail(callInfo, appendLog);
    }


}
