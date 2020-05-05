package com.archer.service_governance.beans;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 服务注册中心value值
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServiceCenterValue {
    private String endPoint;
    private String env;
}
