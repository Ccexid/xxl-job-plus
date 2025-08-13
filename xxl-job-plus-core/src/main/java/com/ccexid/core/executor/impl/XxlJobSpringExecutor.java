package com.ccexid.core.executor.impl;


import com.ccexid.core.executor.XxlJobExecutor;
import com.ccexid.core.glue.GlueFactory;
import com.ccexid.core.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;


/**
 * xxl-job executor (for spring)
 *
 * @author xuxueli 2018-11-01 09:24:52
 */
public class XxlJobSpringExecutor extends XxlJobExecutor implements ApplicationContextAware, SmartInitializingSingleton, DisposableBean {
    private static final Logger logger = LoggerFactory.getLogger(XxlJobSpringExecutor.class);


    // start
    @Override
    public void afterSingletonsInstantiated() {
        try {
            // Initialize job handler repository from methods
            initJobHandlerMethodRepository(applicationContext);
            
            // Refresh GlueFactory
            GlueFactory.refreshInstance(1);
            
            // Start the executor
            super.start();
        } catch (Exception e) {
            throw new RuntimeException("XXL-Job执行器启动失败: " + e.getMessage(), e);
        }
    }

    // destroy
    @Override
    public void destroy() {
        super.destroy();
    }

    private void initJobHandlerMethodRepository(ApplicationContext applicationContext) {
        if (applicationContext == null) {
            return;
        }
        // init job handler from method
        String[] beanDefinitionNames = applicationContext.getBeanNamesForType(Object.class, false, true);
        for (String beanDefinitionName : beanDefinitionNames) {

            // get bean
            Object bean = null;
            Lazy onBean = applicationContext.findAnnotationOnBean(beanDefinitionName, Lazy.class);
            if (onBean!=null){
                logger.debug("xxl-job annotation scan, skip @Lazy Bean:{}", beanDefinitionName);
                continue;
            }else {
                bean = applicationContext.getBean(beanDefinitionName);
            }

            // Filter methods with XxlJob annotation
            Map<Method, XxlJob> annotatedMethods = null;
            try {
                annotatedMethods = MethodIntrospector.selectMethods(bean.getClass(),
                        (MethodIntrospector.MetadataLookup<XxlJob>) method -> AnnotatedElementUtils.findMergedAnnotation(method, XxlJob.class));
            } catch (Throwable ex) {
                logger.error("xxl-job method-jobHandler resolve error for bean[{}].", beanDefinitionName, ex);
            }

            if (annotatedMethods != null && !annotatedMethods.isEmpty()) {
                // generate and regist method job handler
                for (Map.Entry<Method, XxlJob> methodXxlJobEntry : annotatedMethods.entrySet()) {
                    Method executeMethod = methodXxlJobEntry.getKey();
                    XxlJob xxlJob = methodXxlJobEntry.getValue();
                    // regist
                    registerAnnotatedJobHandler(xxlJob, bean, executeMethod);
                }
            }

        }
    }

    // ---------------------- applicationContext ----------------------
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        XxlJobSpringExecutor.applicationContext = Objects.requireNonNull(applicationContext, "ApplicationContext不能为空");
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

}
