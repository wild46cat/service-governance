package com.archer.servgovernspringgrpc.boot.grpc.server.server;


import java.util.List;

public interface GrpcServiceDiscoverer {

    List<GrpcServiceDefinition> findGrpcServices();
}
