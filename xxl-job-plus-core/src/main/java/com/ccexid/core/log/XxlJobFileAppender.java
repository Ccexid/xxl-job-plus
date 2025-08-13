package com.ccexid.core.log;


import com.ccexid.core.service.model.LogResult;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

/**
 * 任务日志文件处理器
 * 负责日志文件的创建、写入和读取，采用JDK8线程安全的日期处理和NIO文件操作
 *
 * @author xuxueli 2016-3-12 19:25:12
 */
public class XxlJobFileAppender {
    private static final Logger logger = LoggerFactory.getLogger(XxlJobFileAppender.class);

    /**
     * 日志基础路径
     */
    private static String logBasePath = "/data/app-logs/xxl-job/job-handler";
    @Getter
    private static String glueSrcPath = logBasePath + "/glue-source";

    /**
     * 线程安全的日期格式化器（JDK8新增，替代SimpleDateFormat）
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 初始化日志路径
     *
     * @param logPath 日志基础路径
     */
    public static void initLogPath(String logPath) {
        // 处理空路径情况
        String resolvedPath = (logPath != null && !logPath.trim().isEmpty()) ? logPath.trim() : logBasePath;

        // 使用NIO创建目录（支持多级目录创建）
        Path baseDirPath = Paths.get(resolvedPath);
        try {
            Files.createDirectories(baseDirPath);
            logBasePath = baseDirPath.toAbsolutePath().toString();
        } catch (IOException e) {
            logger.error("创建日志基础目录失败: {}", resolvedPath, e);
            return;
        }

        // 初始化胶水代码目录
        Path glueDirPath = baseDirPath.resolve("glue-source");
        try {
            Files.createDirectories(glueDirPath);
            glueSrcPath = glueDirPath.toAbsolutePath().toString();
        } catch (IOException e) {
            logger.error("创建胶水代码目录失败: {}", glueDirPath, e);
        }
    }

    public static String getLogPath() {
        return logBasePath;
    }

    /**
     * 生成日志文件完整路径（使用JDK8日期API）
     *
     * @param triggerDate 触发时间（Date类型兼容）
     * @param logId       日志ID
     * @return 日志文件完整路径
     */
    public static String makeLogFileName(Date triggerDate, long logId) {
        // 将Date转换为LocalDate（线程安全）
        LocalDate localDate = triggerDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        String dateDir = localDate.format(DATE_FORMATTER);

        // 构建每日日志目录
        Path dailyLogDir = Paths.get(logBasePath).resolve(dateDir);
        try {
            Files.createDirectories(dailyLogDir);
        } catch (IOException e) {
            logger.error("创建每日日志目录失败: {}", dailyLogDir, e);
        }

        // 生成日志文件路径
        return dailyLogDir.resolve(logId + ".log").toAbsolutePath().toString();
    }

    /**
     * 追加日志内容到文件（使用NIO和try-with-resources）
     *
     * @param logFileName 日志文件路径
     * @param appendLog   要追加的日志内容
     */
    public static void appendLog(String logFileName, String appendLog) {
        if (logFileName == null || logFileName.trim().isEmpty()) {
            logger.warn("日志文件名不能为空，跳过日志写入");
            return;
        }

        Path logFilePath = Paths.get(logFileName);

        // 确保文件存在
        if (!Files.exists(logFilePath)) {
            try {
                Files.createFile(logFilePath);
            } catch (IOException e) {
                logger.error("创建日志文件失败: {}", logFilePath, e);
                return;
            }
        }

        // 处理日志内容，添加系统换行符（跨平台兼容）
        String logContent = (appendLog == null ? "" : appendLog) + System.lineSeparator();

        // 使用NIO的BufferedWriter和try-with-resources自动关闭资源
        try (BufferedWriter writer = Files.newBufferedWriter(
                logFilePath,
                StandardCharsets.UTF_8,
                StandardOpenOption.APPEND)) {
            writer.write(logContent);
        } catch (IOException e) {
            logger.error("追加日志到文件失败: {}", logFileName, e);
        }
    }

    /**
     * 读取日志文件内容（使用NIO和try-with-resources）
     *
     * @param logFileName 日志文件路径
     * @param fromLineNum 起始行号（从1开始）
     * @return 日志读取结果
     */
    public static LogResult readLog(String logFileName, int fromLineNum) {
        if (logFileName == null || logFileName.trim().isEmpty()) {
            return new LogResult(fromLineNum, 0, "日志文件路径为空", true);
        }

        Path logFilePath = Paths.get(logFileName);
        if (!Files.exists(logFilePath)) {
            return new LogResult(fromLineNum, 0, "日志文件不存在: " + logFileName, true);
        }

        StringBuilder logContent = new StringBuilder();
        int toLineNum = 0;

        // 使用NIO的BufferedReader和try-with-resources
        try (LineNumberReader reader = new LineNumberReader(
                Files.newBufferedReader(logFilePath, StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                toLineNum = reader.getLineNumber();
                if (toLineNum >= fromLineNum) {
                    logContent.append(line).append(System.lineSeparator());
                }
            }
        } catch (IOException e) {
            logger.error("读取日志文件失败: {}", logFileName, e);
            return new LogResult(fromLineNum, toLineNum, "读取日志异常: " + e.getMessage(), true);
        }

        return new LogResult(fromLineNum, toLineNum, logContent.toString(), false);
    }

    /**
     * 读取日志文件的所有内容（使用JDK8的Files.readAllLines）
     *
     * @param logFile 日志文件对象
     * @return 日志文件的完整内容（null表示读取失败）
     */
    public static String readLines(File logFile) {
        if (logFile == null || !logFile.exists()) {
            logger.warn("日志文件不存在: {}", logFile);
            return null;
        }

        try {
            // 使用JDK8的Files.readAllLines一次性读取所有行
            List<String> lines = Files.readAllLines(logFile.toPath(), StandardCharsets.UTF_8);
            return String.join(System.lineSeparator(), lines);
        } catch (IOException e) {
            logger.error("读取日志文件内容失败: {}", logFile.getAbsolutePath(), e);
            return null;
        }
    }
}
