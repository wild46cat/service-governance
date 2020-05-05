package com.archer.servgovernspringgrpc.boot.grpc.client.autoconfig;

import com.archer.servgovernspringgrpc.boot.grpc.client.channelfactory.GrpcChannelFactory;
import com.archer.servgovernspringgrpc.boot.grpc.client.channelfactory.NettyChannelFactory;
import com.archer.servgovernspringgrpc.boot.grpc.client.client.GrpcClientBeanPostProcessor;
import com.archer.servgovernspringgrpc.governance.config.GovernanceConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;

@Configuration
public class GrpcClientAutoConfiguration {
    @Bean
    public static GrpcClientBeanPostProcessor grpcClientBeanPostProcessor(final ApplicationContext applicationContext) {
        return new GrpcClientBeanPostProcessor(applicationContext);
    }

    @Bean
    public GrpcChannelFactory nettyGrpcChannelFactory(GovernanceConfig governanceConfig) {
        final NettyChannelFactory channelFactory = new NettyChannelFactory(governanceConfig);
        return channelFactory;
    }
}
