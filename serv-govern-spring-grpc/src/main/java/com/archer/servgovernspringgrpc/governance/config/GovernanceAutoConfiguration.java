package com.archer.servgovernspringgrpc.governance.config;

import io.etcd.jetcd.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GovernanceAutoConfiguration {

    @Bean
    public GovernanceConfig governanceConfig() {
        return new GovernanceConfig();
    }

}
