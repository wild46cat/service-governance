package com.archer.servgovernspringgrpc.boot.grpc.server.serverfactory;

import com.archer.servgovernspringgrpc.boot.grpc.server.server.GrpcServiceDefinition;
import io.grpc.ServerBuilder;

import java.util.List;

public class InProcessGrpcServerFactory extends AbstractGrpcServerFactory {
    public InProcessGrpcServerFactory(List<GrpcServiceDefinition> list) {
        super(list);
    }

    @Override
    protected ServerBuilder newServerBuilder() {
        return ServerBuilder.forPort(super.getPort());
    }
}
