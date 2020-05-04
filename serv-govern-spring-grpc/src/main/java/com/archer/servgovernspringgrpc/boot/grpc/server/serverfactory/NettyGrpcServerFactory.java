package com.archer.servgovernspringgrpc.boot.grpc.server.serverfactory;

import com.archer.servgovernspringgrpc.boot.grpc.server.server.GrpcServiceDefinition;
import io.grpc.ServerBuilder;
import io.grpc.netty.NettyServerBuilder;

import java.util.List;


public class NettyGrpcServerFactory extends AbstractGrpcServerFactory {
    public NettyGrpcServerFactory(List<GrpcServiceDefinition>  list) {
        super(list);
    }

    @Override
    protected ServerBuilder newServerBuilder() {
        return NettyServerBuilder.forPort(super.getPort());
    }
}
