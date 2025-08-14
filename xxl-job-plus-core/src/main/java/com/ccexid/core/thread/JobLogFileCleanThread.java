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

    public static JobLogFileCleanThread getInstance() {
        return INSTANCE;
    }

    private Thread localThread;
    private volatile boolean toStop = false;

    public void start(final long logRetentionDays) {

        // limit min value
        if (logRetentionDays < 3) {
            return;
        }

        localThread = new Thread(() -> {
            while (!toStop) {
                try {
                    // clean log dir, over logRetentionDays
                    File[] childDirs = new File(JobLogFileAppender.getLogPath()).listFiles();
                    if (childDirs != null && childDirs.length > 0) {

                        // today
                        Calendar todayCal = Calendar.getInstance();
                        todayCal.set(Calendar.HOUR_OF_DAY, 0);
                        todayCal.set(Calendar.MINUTE, 0);
                        todayCal.set(Calendar.SECOND, 0);
                        todayCal.set(Calendar.MILLISECOND, 0);

                        Date todayDate = todayCal.getTime();

                        for (File childFile : childDirs) {

                            // valid
                            if (!childFile.isDirectory()) {
                                continue;
                            }
                            if (!childFile.getName().contains("-")) {
                                continue;
                            }

                            // file create date
                            Date logFileCreateDate = DateUtil.parse(childFile.getName(), DATE_FORMAT_PATTERN);
                            if (logFileCreateDate == null) {
                                continue;
                            }

                            if ((todayDate.getTime() - logFileCreateDate.getTime()) >= logRetentionDays * ONE_DAY_MILLISECONDS) {
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
                    TimeUnit.DAYS.sleep(1);
                } catch (InterruptedException e) {
                    if (!toStop) {
                        log.error("Sleep interrupted", e);
                    }
                    // Restore interrupted state
                    Thread.currentThread().interrupt();
                }
            }
            log.info(">>>>>>>>>>> xxl-job, executor JobLogFileCleanThread thread destroy.");

        });
        localThread.setDaemon(true);
        localThread.setName("xxl-job, executor JobLogFileCleanThread");
        localThread.start();
    }


    @Override
    public void toStop() {
        toStop = true;

        if (localThread == null) {
            return;
        }

        // interrupt and wait
        localThread.interrupt();
        try {
            localThread.join();
        } catch (InterruptedException e) {
            log.error("Stop thread error", e);
            // Restore interrupted state
            Thread.currentThread().interrupt();
        }
    }

}
