package com.ccexid.core.annotation;

import com.ccexid.core.configuration.JobPlusAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用 XXL-Job 执行器功能的注解
 * 标注在 Spring Boot 启动类上，用于自动配置 XXL-Job 相关组件
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(JobPlusAutoConfiguration.class)
public @interface EnableJobPlus {
}
