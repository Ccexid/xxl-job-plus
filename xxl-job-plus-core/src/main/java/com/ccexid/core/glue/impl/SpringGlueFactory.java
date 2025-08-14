package com.ccexid.core.glue.impl;


import com.ccexid.core.executor.impl.JobSpringExecutor;
import com.ccexid.core.glue.GlueFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.AnnotationUtils;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * @author xuxueli 2018-11-01
 */
@Slf4j
public class SpringGlueFactory extends GlueFactory {


    /**
     * inject action of spring
     * @param instance
     */
    @Override
    public void injectService(Object instance){
        if (instance == null) {
            return;
        }

        if (JobSpringExecutor.getContext() == null) {
            return;
        }

        Field[] fields = instance.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            Object fieldBean = null;
            // with bean-id, bean could be found by both @Resource and @Autowired, or bean could only be found by @Autowired

            if (AnnotationUtils.getAnnotation(field, Resource.class) != null) {
                try {
                    Resource resource = AnnotationUtils.getAnnotation(field, Resource.class);
                    if (StringUtils.isNotBlank(resource.name())) {
                        fieldBean = JobSpringExecutor.getContext().getBean(resource.name());
                    } else {
                        fieldBean = JobSpringExecutor.getContext().getBean(field.getName());
                    }
                } catch (Exception e) {
                    log.error("Failed to inject resource field: {}", field.getName(), e);
                }
                if (fieldBean == null) {
                    fieldBean = JobSpringExecutor.getContext().getBean(field.getType());
                }
            } else if (AnnotationUtils.getAnnotation(field, Autowired.class) != null) {
                Qualifier qualifier = AnnotationUtils.getAnnotation(field, Qualifier.class);
                if (qualifier != null && StringUtils.isNotBlank(qualifier.value())) {
                    fieldBean = JobSpringExecutor.getContext().getBean(qualifier.value());
                } else {
                    fieldBean = JobSpringExecutor.getContext().getBean(field.getType());
                }
            }

            if (fieldBean != null) {
                field.setAccessible(true);
                try {
                    field.set(instance, fieldBean);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    log.error("Failed to inject field: {}", field.getName(), e);
                }
            }
        }
    }

}
