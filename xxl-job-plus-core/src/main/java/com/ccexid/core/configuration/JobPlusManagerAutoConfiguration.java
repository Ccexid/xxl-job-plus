package com.ccexid.core.configuration;

import com.ccexid.core.annotation.EnableJobAdmin;
import com.ccexid.core.props.JobPlusManagerProperties;
import com.ccexid.core.scheduler.JobScheduler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@AutoConfiguration
@ConditionalOnClass(EnableJobAdmin.class)
@EnableConfigurationProperties(JobPlusManagerProperties.class)
@Slf4j
public class JobPlusManagerAutoConfiguration implements InitializingBean, DisposableBean {

    private static volatile JobPlusManagerAutoConfiguration autoConfiguration = null; // 增加volatile保证可见性
    private final JobPlusManagerProperties properties;
    private JobScheduler jobScheduler;

    public JobPlusManagerAutoConfiguration(JobPlusManagerProperties properties) {
        this.properties = properties;
    }

    public static JobPlusManagerAutoConfiguration getAdminConfig() {
        return autoConfiguration;
    }

    @Override
    public void destroy() throws Exception {
        autoConfiguration = null; // 清理静态引用避免内存泄漏
        jobScheduler.destroy();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        autoConfiguration = this;
        log.info("[JobPlusManagerAutoConfiguration] init plus manager config: {}", properties);
        jobScheduler =  new JobScheduler();
        jobScheduler.init();
    }

    public JobPlusManagerProperties plusManagerConfig() {
        return properties;
    }
}
