package com.archer.servgovernspringgrpc.boot.grpc.server.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("grpc.server")
public class GrpcServerConfig {

    /**
     * grpc 端口
     */
    private int port = 50051;

    /**
     * 服务ID
     */
    private String id = "DEFAULT-SERVICE-ID";


    /**
     * 服务名称
     */
    private String name = "DEFAULT-SERVICE-NAME";


}
