package com.ccexid.core.executor;

import com.ccexid.core.annotation.XxlJob;
import com.ccexid.core.log.JobLogFileAppender;
import com.ccexid.core.props.JobPlusProperties;

import java.lang.reflect.Method;

public class JobExecutor {
    private final JobPlusProperties jobPlusProperties;

    public JobExecutor(JobPlusProperties jobPlusProperties) {
        this.jobPlusProperties = jobPlusProperties;
    }

    public void start() throws Exception {
        //  初始化日志路径
        JobLogFileAppender.initLogPath(jobPlusProperties.getExecutor().getLogPath());
    }

    public void destroy() throws Exception {
    }

    protected void registerJobHandler(XxlJob xxlJob, Object bean, Method executeMethod) {

    }
}
