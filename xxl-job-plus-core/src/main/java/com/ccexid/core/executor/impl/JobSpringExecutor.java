package com.ccexid.core.executor.impl;

import com.ccexid.core.annotation.XxlJob;
import com.ccexid.core.executor.JobExecutor;
import com.ccexid.core.props.JobPlusProperties;
import lombok.extern.slf4j.Slf4j;
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

/**
 * XXL-Job Spring执行器实现类
 * 负责在Spring环境中初始化和管理XXL-Job任务执行器，扫描并注册带有@XxlJob注解的方法
 *
 * @author xuxueli 2018-11-01 09:24:52
 */
@Slf4j
public class JobSpringExecutor extends JobExecutor
        implements ApplicationContextAware, SmartInitializingSingleton, DisposableBean {

    /**
     * Spring应用上下文
     */
    private static ApplicationContext applicationContext;

    /**
     * 构造函数
     *
     * @param jobPlusProperties 任务执行器配置属性
     */
    public JobSpringExecutor(JobPlusProperties jobPlusProperties) {
        super(jobPlusProperties);
    }

    /**
     * 销毁执行器资源
     *
     * @throws Exception 销毁过程中可能抛出的异常
     */
    @Override
    public void destroy() throws Exception {
        super.destroy();
    }

    /**
     * Spring容器完成单例Bean初始化后调用
     * 初始化任务处理器并启动执行器
     */
    @Override
    public void afterSingletonsInstantiated() {
        initJobHandlerMethod(applicationContext);
        try {
            super.start();
        } catch (Exception e) {
            log.error("xxl-job error", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 设置Spring应用上下文
     *
     * @param applicationContext Spring应用上下文
     * @throws BeansException 设置过程中可能抛出的异常
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        JobSpringExecutor.applicationContext = applicationContext;
    }

    /**
     * 获取Spring应用上下文
     *
     * @return Spring应用上下文
     */
    public static ApplicationContext getContext() {
        return applicationContext;
    }

    /**
     * 初始化完成后处理方法
     * 扫描所有Spring Bean中带有@XxlJob注解的方法并注册为任务处理器
     *
     * @param applicationContext Spring应用上下文
     */
    private void initJobHandlerMethod(ApplicationContext applicationContext) {
        if (applicationContext == null) {
            return;
        }
        // 获取所有Bean定义名称
        String[] beanDefinitionNames = applicationContext.getBeanNamesForType(Object.class, false, true);

        for (String beanDefinitionName : beanDefinitionNames) {
            Object bean = null;
            Lazy lazyAnnotation = applicationContext.findAnnotationOnBean(beanDefinitionName, Lazy.class);

            if (lazyAnnotation != null) {
                log.debug("beanDefinitionName:{} is lazy", beanDefinitionName);
                continue;
            } else {
                bean = applicationContext.getBean(beanDefinitionName);
            }
            
            // 查找带有@XxlJob注解的方法
            Map<Method, XxlJob> annotatedMethods = null;
            try {
                annotatedMethods = MethodIntrospector.selectMethods(bean.getClass(), 
                    (MethodIntrospector.MetadataLookup<XxlJob>) method -> 
                        AnnotatedElementUtils.findMergedAnnotation(method, XxlJob.class));
            } catch (Throwable e) {
                log.error("xxl-job method-error", e);
            }

            if (annotatedMethods == null || annotatedMethods.isEmpty()) {
                continue;
            }

            // 注册任务处理器
            for (Map.Entry<Method, XxlJob> methodXxlJobEntry : annotatedMethods.entrySet()) {
                Method executeMethod = methodXxlJobEntry.getKey();
                XxlJob xxlJob = methodXxlJobEntry.getValue();
                registerJobHandler(xxlJob, bean, executeMethod);
            }
        }
    }
}