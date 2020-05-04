package com.archer.servgovernspringgrpc.boot.grpc.server.server;

import io.grpc.ServerServiceDefinition;

public class GrpcServiceDefinition {
    private final String beanName;
    private final Class<?> beanClazz;
    private final ServerServiceDefinition definition;

    public GrpcServiceDefinition(String beanName, Class<?> beanClazz, ServerServiceDefinition definition) {
        this.beanName = beanName;
        this.beanClazz = beanClazz;
        this.definition = definition;
    }

    public String getBeanName() {
        return beanName;
    }

    public Class<?> getBeanClazz() {
        return beanClazz;
    }

    public ServerServiceDefinition getDefinition() {
        return definition;
    }
}
