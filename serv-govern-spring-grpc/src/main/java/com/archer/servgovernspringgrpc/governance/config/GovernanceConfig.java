package com.archer.servgovernspringgrpc.governance.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@ConfigurationProperties("goverance")
public class GovernanceConfig {

    /**
     * etcd地址,多个用逗号分隔
     */
    private String etcdEndpoints = "http://localhost:2379";

    /**
     * 开启debug模式(client指定解析)
     */
    private boolean clientDebugFlag = false;

    /**
     * 具体debug信息
     */
    private Map<String, List<String>> ClientDebugInfo = new HashMap<>();
}
