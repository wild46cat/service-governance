package com.archer.service_governance;

import com.archer.service_governance.beans.ServiceEntity;
import com.archer.service_governance.register.ServiceRegister;
import io.etcd.jetcd.Client;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 服务治理客户端
 */
public class ServiceGovernanceClient {
    /**
     * etcd client
     */
    private Client client;

    /**
     * etcd地址，逗号分隔
     */
    private String etcdEndPoints = "http://localhost:2379";

    private List<ServiceEntity> serviceEntityList = new ArrayList<>();

    private List<ServiceRegister> serviceRegisterList = new ArrayList<>();

    public ServiceGovernanceClient(String etcdClientAddrs, ServiceEntity... serviceEntity) {
        if (StringUtils.isNotBlank(etcdClientAddrs)) {
            this.etcdEndPoints = etcdClientAddrs;
        }
        String[] strings = etcdEndPoints.split(",");
        client = Client.builder().endpoints(strings).build();

        this.serviceEntityList = Arrays.asList(serviceEntity);
        for (ServiceEntity entity : this.serviceEntityList) {
            ServiceRegister serviceRegister = new ServiceRegister(client, entity);
            this.serviceRegisterList.add(serviceRegister);
        }
    }

    public Client getClient() {
        return client;
    }

    /**
     * 程序启动,注册服务
     */
    public void start() {
        for (ServiceRegister register : serviceRegisterList) {
            register.register();
        }
        Runtime.getRuntime().addShutdownHook(new Thread(
                () -> {
                    shutdown();
                })
        );
    }

    /**
     * 程序关闭,注销服务
     */
    private void shutdown() {
        for (ServiceRegister serviceRegister : serviceRegisterList) {
            serviceRegister.unRegister();
        }
    }

}
