package com.ccexid.core.log;

import com.ccexid.core.model.LogResult;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 任务日志文件追加器
 * 负责管理任务执行日志和Glue源码的文件路径，并确保相关目录存在
 *
 * @author xuxueli 2018-11-01 09:24:52
 */
@Slf4j
public class JobLogFileAppender {
    private static volatile String logBasePath = "/data/app-logs/xxl-job/job-handler";
    private static final String GLUE_DIR_NAME = "glue-source";
    private static volatile String glueSrcPath = logBasePath.concat("/glue-source");
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    /**
     * 获取日志基础路径
     *
     * @return 日志基础路径
     */
    public static String getLogPath() {
        return logBasePath;
    }

    /**
     * 获取Glue源码路径
     *
     * @return Glue源码路径
     */
    public static String getGlueSrcPath() {
        return glueSrcPath;
    }

    /**
     * 初始化日志路径
     * 如果指定的日志路径不为空，则使用指定路径，否则使用默认路径
     * 确保基础日志目录和Glue源码目录存在，如果不存在则创建
     *
     * @param logPath 指定的日志路径，如果为null或空字符串则使用默认路径
     */
    public static void initLogPath(String logPath) {
        // init
        if (logPath != null && !logPath.trim().isEmpty()) {
            logBasePath = logPath;
        }
        // mk base dir
        File logPathDir = new File(logBasePath);
        if (!logPathDir.exists()) {
            if (!logPathDir.mkdirs()) {
                log.error("创建日志基础目录失败: {}", logBasePath);
            }
        }
        logBasePath = logPathDir.getPath();

        // mk glue dir
        File glueBaseDir = new File(logPathDir, GLUE_DIR_NAME);
        if (!glueBaseDir.exists()) {
            if (!glueBaseDir.mkdirs()) {
                log.error("创建Glue源码目录失败: {}", glueBaseDir.getPath());
            }
        }
        glueSrcPath = glueBaseDir.getPath();
    }

    /**
     * 生成日志文件名
     * 根据触发时间和日志ID生成日志文件路径，格式为: 基础路径/日期/日志ID.log
     *
     * @param triggerDate 触发时间
     * @param logId       日志ID
     * @return 日志文件名（完整路径）
     */
    public static String makeLogFileName(Date triggerDate, long logId) {
        // filePath/yyyy-MM-dd
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);    // avoid concurrent problem, can not be static
        String dateStr = sdf.format(triggerDate);
        File logFilePath = new File(getLogPath(), dateStr);
        if (!logFilePath.exists()) {
            if (!logFilePath.mkdirs()) {
                log.error("创建日志文件目录失败: {}", logFilePath.getPath());
            }
        }

        // filePath/yyyy-MM-dd/9999.log
        return String.format("%s%s%d.log", logFilePath.getPath(), File.separator, logId);
    }

    /**
     * 追加日志到指定文件
     * 如果文件不存在则创建文件，如果父目录不存在则创建父目录
     *
     * @param logFileName 日志文件名（完整路径）
     * @param appendLog   要追加的日志内容
     */
    public static void appendLog(String logFileName, String appendLog) {
        // log file
        if (logFileName == null || logFileName.trim().isEmpty()) {
            return;
        }
        File logFile = new File(logFileName);

        // create parent dir if not exists
        File parentDir = logFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                log.error("创建日志文件父目录失败: {}", parentDir.getPath());
                return;
            }
        }

        if (!logFile.exists()) {
            try {
                if (!logFile.createNewFile()) {
                    log.error("创建日志文件失败: {}", logFileName);
                    return;
                }
            } catch (IOException e) {
                log.error("创建日志文件异常: {}", logFileName, e);
                return;
            }
        }

        // log
        if (appendLog == null) {
            appendLog = "";
        }
        appendLog += "\r\n";

        // append file content
        try (FileOutputStream fos = new FileOutputStream(logFile, true)) {
            fos.write(appendLog.getBytes(StandardCharsets.UTF_8));
            fos.flush();
        } catch (Exception e) {
            log.error("写入日志文件异常: {}", logFileName, e);
        }
    }


    /**
     * 读取日志文件内容
     *
     * @param logFile 日志文件
     * @return 日志内容，每行以换行符分隔
     */
    public static String readLines(File logFile) {
        if (logFile == null || !logFile.exists()) {
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(logFile.toPath()), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }
            return sb.toString();
        } catch (IOException e) {
            log.error("读取日志文件异常: {}", logFile.getAbsolutePath(), e);
        } catch (OutOfMemoryError e) {
            log.error("读取日志文件内存溢出: {}", logFile.getAbsolutePath(), e);
        }
        return null;
    }

    /**
     * 从指定行号开始读取日志文件内容
     *
     * @param logFileName 日志文件名
     * @param fromLineNum 起始行号（从1开始）
     * @return 日志结果对象，包含读取的行号范围和日志内容
     */
    public static LogResult readLog(String logFileName, int fromLineNum) {
        if (logFileName == null || logFileName.trim().isEmpty()) {
            return new LogResult(fromLineNum, 0, "readLog fail, logFile not found", true);
        }
        File logFile = new File(logFileName);

        if (!logFile.exists()) {
            return new LogResult(fromLineNum, 0, "readLog fail, logFile not exists", true);
        }

        // read file
        StringBuilder logContentBuffer = new StringBuilder();
        int toLineNum = 0;
        
        try (LineNumberReader reader = new LineNumberReader(
                new InputStreamReader(Files.newInputStream(logFile.toPath()), StandardCharsets.UTF_8))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                toLineNum = reader.getLineNumber(); // [from, to], start as 1
                if (toLineNum >= fromLineNum) {
                    logContentBuffer.append(line).append("\n");
                }
            }
        } catch (IOException e) {
            log.error("读取日志文件异常: {}", logFile.getAbsolutePath(), e);
            return new LogResult(fromLineNum, 0, "readLog fail, IOException", true);
        }

        // result
        return new LogResult(fromLineNum, toLineNum, logContentBuffer.toString(), true);
    }
}
