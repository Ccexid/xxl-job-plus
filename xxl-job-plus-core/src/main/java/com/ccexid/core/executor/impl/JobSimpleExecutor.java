package com.ccexid.core.executor.impl;

import com.ccexid.core.annotation.XxlJob;
import com.ccexid.core.executor.JobExecutor;
import com.ccexid.core.props.JobPlusProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


/**
 * xxl-job executor (for frameless)
 *
 * @author xuxueli 2020-11-05
 */
@Slf4j
public class JobSimpleExecutor extends JobExecutor {
    private List<Object> jobBeanList = new ArrayList<>();

    /**
     * 构造函数
     *
     * @param jobPlusProperties 任务配置属性对象
     */
    public JobSimpleExecutor(JobPlusProperties jobPlusProperties) {
        super(jobPlusProperties);
    }

    public List<Object> getXxlJobBeanList() {
        return jobBeanList;
    }
    public void setXxlJobBeanList(List<Object> xxlJobBeanList) {
        this.jobBeanList = xxlJobBeanList;
    }


    @Override
    public void start() {

        // init JobHandler Repository (for method)
        initJobHandlerMethodRepository(jobBeanList);

        // super start
        try {
            super.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void destroy() throws Exception {
        super.destroy();
    }


    private void initJobHandlerMethodRepository(List<Object> xxlJobBeanList) {
        if (CollectionUtils.isEmpty(xxlJobBeanList)) {
            return;
        }

        // init job handler from method
        for (Object bean : xxlJobBeanList) {
            // method
            Method[] methods = bean.getClass().getDeclaredMethods();
            if (methods.length == 0) {
                continue;
            }
            for (Method executeMethod : methods) {
                XxlJob xxlJob = executeMethod.getAnnotation(XxlJob.class);
                // registry
                registerJobHandler(xxlJob, bean, executeMethod);
            }
        }
    }

}
