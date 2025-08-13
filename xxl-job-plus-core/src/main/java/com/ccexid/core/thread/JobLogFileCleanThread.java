package com.ccexid.core.thread;

import com.ccexid.core.log.XxlJobFileAppender;
import com.ccexid.core.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.concurrent.TimeUnit;
import java.util.function.BiPredicate;

/**
 * 任务日志文件清理线程
 * 定期清理超过保留天数的日志文件，避免磁盘空间耗尽
 *
 * @author xuxueli 2017-12-29 16:23:43
 */
public class JobLogFileCleanThread {
    private static final Logger logger = LoggerFactory.getLogger(JobLogFileCleanThread.class);
    private static final JobLogFileCleanThread INSTANCE = new JobLogFileCleanThread();
    private static final int MIN_RETENTION_DAYS = 3; // 最小日志保留天数
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private Thread cleanThread;
    private volatile boolean isStopping = false;
    private long cleanIntervalHours = 24; // 清理间隔时间（小时）
    
    // 日志目录验证策略，提高可扩展性
    private BiPredicate<File, String> logDirectoryValidator = (file, name) -> file.isDirectory() && name.contains("-");

    /**
     * 获取单例实例
     */
    public static JobLogFileCleanThread getInstance() {
        return INSTANCE;
    }

    /**
     * 启动日志清理线程
     *
     * @param logRetentionDays 日志保留天数（小于3天则不启动）
     */
    public void start(final long logRetentionDays) {
        // 校验保留天数，小于最小值则不启动
        if (logRetentionDays < MIN_RETENTION_DAYS) {
            return;
        }

        cleanThread = new Thread(() -> {
            while (!isStopping) {
                try {
                    // 执行日志清理
                    cleanExpiredLogFiles(logRetentionDays);
                } catch (Throwable e) {
                    if (!isStopping) {
                        logger.error("日志清理线程执行异常", e);
                    }
                }

                // 每天执行一次清理
                try {
                    TimeUnit.HOURS.sleep(cleanIntervalHours);
                } catch (InterruptedException e) {
                    if (!isStopping) {
                        logger.error("日志清理线程等待被中断", e);
                    }
                }
            }
            logger.info(">>>>>>>>>>> xxl-job, 执行器日志文件清理线程已销毁");
        }, "xxl-job, executor JobLogFileCleanThread");

        cleanThread.setDaemon(true);
        cleanThread.start();
    }

    /**
     * 停止日志清理线程
     */
    public void stop() {
        isStopping = true;

        if (cleanThread == null) {
            return;
        }

        // 中断并等待线程停止
        cleanThread.interrupt();
        try {
            cleanThread.join();
        } catch (InterruptedException e) {
            logger.error("日志清理线程停止失败", e);
        }
    }

    /**
     * 清理过期的日志文件
     *
     * @param retentionDays 日志保留天数
     */
    private void cleanExpiredLogFiles(long retentionDays) {
        File logDir = new File(XxlJobFileAppender.getLogPath());
        File[] dateDirs = logDir.listFiles();

        if (dateDirs == null || dateDirs.length == 0) {
            return;
        }

        // 获取当前日期（午夜时间点）
        LocalDate today = LocalDate.now();

        for (File dir : dateDirs) {
            // 只处理目录且名称符合日期格式的文件夹
            if (isValidLogDirectory(dir)) {
                try {
                    // 解析目录名称为日期
                    LocalDate dirDate = LocalDate.parse(dir.getName(), DATE_FORMATTER);

                    // 计算与当前日期的差值
                    long daysDiff = java.time.temporal.ChronoUnit.DAYS.between(dirDate, today);

                    // 如果超过保留天数则删除
                    if (daysDiff >= retentionDays) {
                        FileUtils.deleteRecursively(dir);
                        logger.debug("清理过期日志目录: {}", dir.getAbsolutePath());
                    }
                } catch (DateTimeParseException e) {
                    logger.warn("日志目录名称格式无效: {}", dir.getName(), e);
                }
            }
        }
    }

    /**
     * 验证是否为有效的日志目录
     *
     * @param file 待验证的文件
     * @return 是否为有效的日志目录
     */
    private boolean isValidLogDirectory(File file) {
        return logDirectoryValidator.test(file, file.getName());
    }
    
    /**
     * 设置清理间隔时间
     * @param cleanIntervalHours 清理间隔时间（小时）
     */
    public void setCleanIntervalHours(long cleanIntervalHours) {
        this.cleanIntervalHours = cleanIntervalHours;
    }
    
    /**
     * 设置日志目录验证策略
     * @param logDirectoryValidator 日志目录验证策略
     */
    public void setLogDirectoryValidator(BiPredicate<File, String> logDirectoryValidator) {
        this.logDirectoryValidator = logDirectoryValidator;
    }
    
    /**
     * 检查线程是否正在运行
     * @return 是否正在运行
     */
    public boolean isRunning() {
        return cleanThread != null && cleanThread.isAlive() && !isStopping;
    }
}