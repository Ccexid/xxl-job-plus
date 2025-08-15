package com.ccexid.core.thread;

import com.ccexid.core.log.JobLogFileAppender;
import com.ccexid.core.util.DateUtil;
import com.ccexid.core.util.FileUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * job file clean thread
 *
 * @author xuxueli 2017-12-29 16:23:43
 */
@Slf4j
public class JobLogFileCleanThread implements IThread {

    private static final JobLogFileCleanThread INSTANCE = new JobLogFileCleanThread();

    private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd";
    private static final long ONE_DAY_MILLISECONDS = 24 * 60 * 60 * 1000L;
    private long logRetentionDays;

    /**
     * 获取单例实例
     *
     * @return JobLogFileCleanThread 单例对象
     */
    public static JobLogFileCleanThread getInstance() {
        return INSTANCE;
    }

    private Thread localThread;
    private volatile boolean toStop = false;

    /**
     * 启动日志文件清理线程
     *
     * @param logRetentionDays 日志保留天数，小于3时不会启动清理任务
     */
    public void start(final long logRetentionDays) {

        // 限制最小值，避免误删重要日志
        if (logRetentionDays < 3) {
            return;
        }
        this.logRetentionDays = logRetentionDays;
        start();

    }


    @Override
    public void start() {
        localThread = new Thread(() -> {
            while (!toStop) {
                try {
                    // 清理超过保留天数的日志目录
                    File[] childDirs = new File(JobLogFileAppender.getLogPath()).listFiles();
                    if (childDirs != null && childDirs.length > 0) {

                        // 获取今天的起始时间（00:00:00）
                        Calendar todayCal = Calendar.getInstance();
                        todayCal.set(Calendar.HOUR_OF_DAY, 0);
                        todayCal.set(Calendar.MINUTE, 0);
                        todayCal.set(Calendar.SECOND, 0);
                        todayCal.set(Calendar.MILLISECOND, 0);

                        Date todayDate = todayCal.getTime();

                        for (File childFile : childDirs) {

                            // 跳过非目录文件和不包含日期格式的文件夹
                            if (!childFile.isDirectory()) {
                                continue;
                            }
                            if (!childFile.getName().contains("-")) {
                                continue;
                            }

                            // 解析文件夹名称中的日期信息
                            Date logFileCreateDate = DateUtil.parse(childFile.getName(), DATE_FORMAT_PATTERN);
                            if (logFileCreateDate == null) {
                                continue;
                            }

                            // 判断是否超过保留天数，若超过则删除该目录
                            if ((todayDate.getTime() - logFileCreateDate.getTime()) >= this.logRetentionDays * ONE_DAY_MILLISECONDS) {
                                if (!FileUtil.deleteRecursively(childFile)) {
                                    log.warn("Delete log file failed: {}", childFile.getAbsolutePath());
                                }
                            }

                        }
                    }

                } catch (Exception e) {
                    if (!toStop) {
                        log.error("Clean log file error", e);
                    }

                }

                try {
                    // 每天执行一次清理操作
                    TimeUnit.DAYS.sleep(1);
                } catch (InterruptedException e) {
                    if (!toStop) {
                        log.error("Sleep interrupted", e);
                    }
                    // 恢复中断状态
                    Thread.currentThread().interrupt();
                }
            }
            log.info(">>>>>>>>>>> xxl-job, executor JobLogFileCleanThread thread destroy.");

        });
        localThread.setDaemon(true);
        localThread.setName("xxl-job, executor JobLogFileCleanThread");
        localThread.start();
    }

    /**
     * 停止当前线程运行
     * 中断线程并等待其结束
     */
    @Override
    public void toStop() {
        toStop = true;

        if (localThread == null) {
            return;
        }

        // 中断并等待线程结束
        localThread.interrupt();
        try {
            localThread.join();
        } catch (InterruptedException e) {
            log.error("Stop thread error", e);
            // 恢复中断状态
            Thread.currentThread().interrupt();
        }
    }

}
