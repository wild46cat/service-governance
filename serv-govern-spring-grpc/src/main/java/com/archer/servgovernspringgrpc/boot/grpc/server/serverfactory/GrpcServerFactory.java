
package com.archer.servgovernspringgrpc.boot.grpc.server.serverfactory;

import org.springframework.beans.factory.DisposableBean;

import io.grpc.Server;

public interface GrpcServerFactory extends DisposableBean {

    Server createServer();

    String getAddress();

    int getPort();

    @Override
    void destroy();

}
