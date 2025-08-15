package com.ccexid.core.configuration.condition;

import com.ccexid.core.annotation.EnableJobAdmin;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.ClassUtils;

/**
 * 自定义条件：检查是否存在 @EnableJobPlus 注解
 */
public class OnEnableJobPlusManagerAnnotationCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
        if (beanFactory == null) {
            return false;
        }
        String[] beanDefinitionNames = beanFactory.getBeanDefinitionNames();
        for (String beanName : beanDefinitionNames) {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
            // 获取Bean的类名
            String beanClassName = beanDefinition.getBeanClassName();
            if (beanClassName == null) {
                continue;
            }
            // 加载类并检查是否有@EnableJobPlus注解
            try {
                Class<?> beanClass = ClassUtils.forName(beanClassName, context.getClassLoader());
                if (beanClass.isAnnotationPresent(EnableJobAdmin.class)) {
                    return true;
                }
            } catch (ClassNotFoundException | LinkageError e) {
                // 忽略类加载失败的情况（可能是代理类或未加载的类）
                continue;
            }
        }
        return false;
    }
}
