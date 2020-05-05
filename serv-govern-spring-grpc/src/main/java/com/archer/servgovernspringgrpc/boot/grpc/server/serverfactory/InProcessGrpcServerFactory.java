package com.archer.servgovernspringgrpc.boot.grpc.server.serverfactory;

import com.archer.servgovernspringgrpc.boot.grpc.server.config.GrpcServerConfig;
import com.archer.servgovernspringgrpc.boot.grpc.server.server.GrpcServiceDefinition;
import io.grpc.ServerBuilder;

import java.util.List;

public class InProcessGrpcServerFactory extends AbstractGrpcServerFactory {
    public InProcessGrpcServerFactory(List<GrpcServiceDefinition> list, GrpcServerConfig config) {
        super(list,config);
    }

    @Override
    protected ServerBuilder newServerBuilder() {
        return ServerBuilder.forPort(super.getPort());
    }
}
