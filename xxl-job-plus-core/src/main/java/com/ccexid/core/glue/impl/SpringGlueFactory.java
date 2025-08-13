package com.ccexid.core.glue.impl;


import com.ccexid.core.executor.impl.XxlJobSpringExecutor;
import com.ccexid.core.glue.GlueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.AnnotationUtils;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Spring胶水工厂实现
 * 负责在Spring环境中注入服务到任务处理器实例中
 *
 * @author xuxueli 2018-11-01
 */
public class SpringGlueFactory extends GlueFactory {
    private static final Logger logger = LoggerFactory.getLogger(SpringGlueFactory.class);


    /**
     * 注入Spring服务到实例中
     * @param instance 需要注入服务的实例
     */
    @Override
    public void injectService(Object instance){
        if (instance == null) {
            return;
        }

        if (XxlJobSpringExecutor.getApplicationContext() == null) {
            logger.warn("ApplicationContext is null, skip inject service for instance: {}", instance.getClass().getName());
            return;
        }

        Field[] fields = instance.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            Object fieldBean = null;
            // 通过@Resource注解注入
            Resource resource = AnnotationUtils.getAnnotation(field, Resource.class);
            if (resource != null) {
                fieldBean = getResourceBean(resource, field);
            } 
            // 通过@Autowired注解注入
            else if (AnnotationUtils.getAnnotation(field, Autowired.class) != null) {
                fieldBean = getAutowiredBean(field);
            }

            if (fieldBean != null) {
                injectField(instance, field, fieldBean);
            }
        }
    }

    /**
     * 获取@Resource注解标记的Bean
     * @param resource Resource注解
     * @param field 字段
     * @return Bean实例
     */
    private Object getResourceBean(Resource resource, Field field) {
        try {
            if (resource.name() != null && !resource.name().isEmpty()) {
                return XxlJobSpringExecutor.getApplicationContext().getBean(resource.name());
            } else {
                return XxlJobSpringExecutor.getApplicationContext().getBean(field.getName());
            }
        } catch (Exception e) {
            logger.debug("Failed to get bean by @Resource name, try to get by type", e);
        }
        
        try {
            return XxlJobSpringExecutor.getApplicationContext().getBean(field.getType());
        } catch (Exception e) {
            logger.debug("Failed to get bean by @Resource type", e);
            return null;
        }
    }

    /**
     * 获取@Autowired注解标记的Bean
     * @param field 字段
     * @return Bean实例
     */
    private Object getAutowiredBean(Field field) {
        try {
            Qualifier qualifier = AnnotationUtils.getAnnotation(field, Qualifier.class);
            if (qualifier != null && qualifier.value() != null && !qualifier.value().isEmpty()) {
                return XxlJobSpringExecutor.getApplicationContext().getBean(qualifier.value());
            } else {
                return XxlJobSpringExecutor.getApplicationContext().getBean(field.getType());
            }
        } catch (Exception e) {
            logger.debug("Failed to get bean by @Autowired", e);
            return null;
        }
    }

    /**
     * 注入字段值
     * @param instance 实例对象
     * @param field 字段
     * @param fieldBean 要注入的Bean
     */
    private void injectField(Object instance, Field field, Object fieldBean) {
        try {
            field.setAccessible(true);
            field.set(instance, fieldBean);
            logger.debug("Successfully injected bean '{}' into field '{}'", 
                    fieldBean.getClass().getName(), field.getName());
        } catch (IllegalArgumentException | IllegalAccessException e) {
            logger.error("Failed to inject bean into field: {}", field.getName(), e);
        }
    }
}