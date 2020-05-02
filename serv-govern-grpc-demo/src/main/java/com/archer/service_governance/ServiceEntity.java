package com.archer.service_governance;

import lombok.Builder;
import lombok.Data;

/**
 * 服务信息
 */
@Data
@Builder
public class ServiceEntity {
    /**
     * serviceId
     */
    private String serviceId = "DEFAULT-SERVICEID";
    /**
     * serviceName
     */
    private String serviceName = "DEFAULT-SERVICE";
    /**
     * host
     */
    private String host = "localhost";
    /**
     * port
     */
    private int port = 8080;
    /**
     * endpoint
     */
    private String endPoint = "localhost:8080";
    /**
     * 协议（http/grpc）
     */
    private ProtocolEnum protocolEnum = ProtocolEnum.gRPC;
    /**
     * 环境信息
     */
    private EnvEnum envEnum = EnvEnum.DEV;
}
