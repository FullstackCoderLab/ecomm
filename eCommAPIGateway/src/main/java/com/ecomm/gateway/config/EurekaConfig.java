package com.ecomm.gateway.config;

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.EurekaInstanceConfig;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.providers.EurekaConfigBasedInstanceInfoProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Fallback configuration ensuring ApplicationInfoManager and InstanceInfo beans
 * are guaranteed to be present for EurekaServerAutoConfiguration across all runtime environments.
 */
@Configuration(proxyBeanMethods = false)
public class EurekaConfig {

    @Bean
    @ConditionalOnMissingBean(InstanceInfo.class)
    public InstanceInfo instanceInfo(EurekaInstanceConfig config) {
        return new EurekaConfigBasedInstanceInfoProvider(config).get();
    }

    @Bean
    @ConditionalOnMissingBean(ApplicationInfoManager.class)
    public ApplicationInfoManager applicationInfoManager(EurekaInstanceConfig config, InstanceInfo instanceInfo) {
        return new ApplicationInfoManager(config, instanceInfo);
    }
}
