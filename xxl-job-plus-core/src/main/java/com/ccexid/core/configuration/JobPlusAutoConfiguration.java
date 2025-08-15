package com.ccexid.core.configuration;

import com.ccexid.core.annotation.EnableJobPlus;
import com.ccexid.core.configuration.condition.OnEnableJobPlusAnnotationCondition;
import com.ccexid.core.executor.JobExecutor;
import com.ccexid.core.executor.impl.JobSpringExecutor;
import com.ccexid.core.props.JobPlusProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;

@AutoConfiguration
@ConditionalOnClass(EnableJobPlus.class)
@Conditional(OnEnableJobPlusAnnotationCondition.class)
@EnableConfigurationProperties(JobPlusProperties.class)
public class JobPlusAutoConfiguration {

    private final JobPlusProperties properties;

    public JobPlusAutoConfiguration(JobPlusProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean
    public JobExecutor jobExecutor() {
        // 初始化配置
        return new JobSpringExecutor(properties);
    }
}
