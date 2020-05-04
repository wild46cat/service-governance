package com.archer.servgovernspringgrpc.boot.grpc.server.serverfactory;

import com.archer.servgovernspringgrpc.boot.grpc.server.server.GrpcServiceDefinition;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractGrpcServerFactory<T extends ServerBuilder<T>> implements GrpcServerFactory {
    private static final Logger log = LoggerFactory.getLogger(AbstractGrpcServerFactory.class);
    protected List<GrpcServiceDefinition> grpcServiceDefinitionList = new ArrayList<GrpcServiceDefinition>();

    public AbstractGrpcServerFactory(List<GrpcServiceDefinition> grpcServiceDefinitionList) {
        this.grpcServiceDefinitionList = grpcServiceDefinitionList;
    }

    @Override
    public Server createServer() {
        ServerBuilder serverBuilder = this.newServerBuilder();
        for (GrpcServiceDefinition serviceDefinition : grpcServiceDefinitionList) {
            log.info(String.format("gRPC Load Service:%s,Bean:%s", serviceDefinition.getBeanClazz().getName(),
                    serviceDefinition.getBeanName()));
            serverBuilder.addService(serviceDefinition.getDefinition());
        }
        return serverBuilder.build();
    }

    @Override
    public String getAddress() {
        InetAddress inetAddress = null;
        try {
            inetAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        if (inetAddress != null) {
            return StringUtils.substringAfterLast(inetAddress.toString(), "/");
        } else {
            return "";
        }
    }

    @Override
    public int getPort() {
        return 50051;
    }

    @Override
    public void destroy() {

    }

    protected abstract T newServerBuilder();

}
