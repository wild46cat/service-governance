package com.archer.servgovernspringgrpc.boot.grpc.server.autoconfig;

import com.archer.servgovernspringgrpc.boot.grpc.server.server.AnnotationGrpcServiceDiscoverer;
import com.archer.servgovernspringgrpc.boot.grpc.server.server.GrpcServiceDefinition;
import com.archer.servgovernspringgrpc.boot.grpc.server.server.GrpcServiceDiscoverer;
import com.archer.servgovernspringgrpc.boot.grpc.server.serverfactory.AbstractGrpcServerFactory;
import com.archer.servgovernspringgrpc.boot.grpc.server.serverfactory.GrpcServerFactory;
import com.archer.servgovernspringgrpc.boot.grpc.server.serverfactory.GrpcServerLifecycle;
import com.archer.servgovernspringgrpc.boot.grpc.server.serverfactory.NettyGrpcServerFactory;
import com.archer.servgovernspringgrpc.governance.EnvEnum;
import com.archer.servgovernspringgrpc.governance.ServiceEntity;
import com.archer.servgovernspringgrpc.governance.ServiceGovernanceClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

import static com.archer.servgovernspringgrpc.governance.ProtocolEnum.gRPC;

@Configuration
public class GrpcAutoconfiguration {

    /**
     * 服务治理bean
     *
     * @return
     */
    @Bean
    public ServiceGovernanceClient serviceGovernanceClient() {
        return new ServiceGovernanceClient("", ServiceEntity.builder()
                .endPoint("localhost:50051").envEnum(EnvEnum.DEV)
                .host("localhost").port(50051).serviceId("dfdf")
                .serviceName("helloworld").protocolEnum(gRPC).build());
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
    public GrpcServerFactory nettyGrpcServerFactory(GrpcServiceDiscoverer discoverer) {
        List<GrpcServiceDefinition> grpcServices = discoverer.findGrpcServices();
        final GrpcServerFactory factory = new NettyGrpcServerFactory(grpcServices);
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
}
