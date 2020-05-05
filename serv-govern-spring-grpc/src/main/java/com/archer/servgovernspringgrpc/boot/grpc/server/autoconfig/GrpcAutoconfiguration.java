package com.archer.servgovernspringgrpc.boot.grpc.server.autoconfig;

import com.archer.servgovernspringgrpc.boot.grpc.server.config.GrpcServerConfig;
import com.archer.servgovernspringgrpc.boot.grpc.server.server.AnnotationGrpcServiceDiscoverer;
import com.archer.servgovernspringgrpc.boot.grpc.server.server.GrpcServiceDefinition;
import com.archer.servgovernspringgrpc.boot.grpc.server.server.GrpcServiceDiscoverer;
import com.archer.servgovernspringgrpc.boot.grpc.server.serverfactory.GrpcServerFactory;
import com.archer.servgovernspringgrpc.boot.grpc.server.serverfactory.GrpcServerLifecycle;
import com.archer.servgovernspringgrpc.boot.grpc.server.serverfactory.NettyGrpcServerFactory;
import com.archer.servgovernspringgrpc.governance.config.GovernanceConfig;
import com.archer.servgovernspringgrpc.governance.enums.EnvEnum;
import com.archer.servgovernspringgrpc.governance.beans.ServiceEntity;
import com.archer.servgovernspringgrpc.governance.ServiceGovernanceClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static com.archer.servgovernspringgrpc.governance.enums.ProtocolEnum.gRPC;

@Configuration
public class GrpcAutoconfiguration {

    @Bean
    public GrpcServerConfig grpcServerConfig() {
        return new GrpcServerConfig();
    }

    /**
     * 注入servcie
     *
     * @return
     */
    @Bean
    public GrpcServiceDiscoverer grpcServiceDiscoverer() {
        return new AnnotationGrpcServiceDiscoverer();
    }

    /**
     * netty server
     *
     * @param discoverer
     * @return
     */
    @Bean
    public GrpcServerFactory nettyGrpcServerFactory(GrpcServiceDiscoverer discoverer, GrpcServerConfig grpcServerConfig) {
        List<GrpcServiceDefinition> grpcServices = discoverer.findGrpcServices();
        final GrpcServerFactory factory = new NettyGrpcServerFactory(grpcServices, grpcServerConfig);
        return factory;
    }

    /**
     * grpc server lifecycle
     *
     * @param factory
     * @return
     */
    @Bean
    public GrpcServerLifecycle grpcServerLifecycle(final GrpcServerFactory factory, ServiceGovernanceClient serviceGovernanceClient) {
        return new GrpcServerLifecycle(factory, serviceGovernanceClient);
    }

    /**
     * 服务治理grpc
     *
     * @return
     */
    @Bean
    public ServiceGovernanceClient serviceGovernanceClient(GrpcServerFactory factory, GrpcServerConfig grpcServerConfig,
                                                           GovernanceConfig governanceConfig) {
        String host = factory.getAddress();
        String endPoint = host + ":" + grpcServerConfig.getPort();
        //指定-Denv=dev
        String env = System.getProperty("env");
        return new ServiceGovernanceClient(governanceConfig.getEtcdEndpoints(), ServiceEntity.builder()
                .endPoint(endPoint).envEnum(EnvEnum.getEnvEnum(env))
                .host(host).port(grpcServerConfig.getPort()).serviceId(grpcServerConfig.getId())
                .serviceName(grpcServerConfig.getName()).protocolEnum(gRPC).build());
    }
}
