package com.archer.service_governance;

import com.alibaba.fastjson.JSON;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.Lease;
import io.etcd.jetcd.lease.LeaseGrantResponse;
import io.etcd.jetcd.lease.LeaseKeepAliveResponse;
import io.etcd.jetcd.lease.LeaseRevokeResponse;
import io.etcd.jetcd.options.PutOption;

import java.util.concurrent.*;

import static sun.rmi.registry.RegistryImpl.getID;

public class ServiceRegister implements Register {
    public static final String serviceRegisterPrefix = "/services";
    public static final String spliter = "/";
    //租约过期时间秒
    public static final int leaseTimeToLive = 10;
    //续租间隔毫秒数
    public static final int KeepAliveIntervalMs = 9000;
    //申请租约超时时间毫秒
    public static final int grantLeaseTimeout = 1000;


    /**
     * etcd client
     */
    private Client client;
    /**
     * 服务对象
     */
    private ServiceEntity serviceEntity;

    /**
     * 租约id
     */
    private long leaseId;

    public ServiceRegister() {
    }

    public ServiceRegister(Client client, ServiceEntity serviceEntity) {
        this.client = client;
        this.serviceEntity = serviceEntity;
    }

    @Override
    public void register() {
        String serviceKey = new StringBuilder(serviceRegisterPrefix)
                .append(spliter)
                .append(serviceEntity.getServiceName())
                .append(spliter)
                .append(serviceEntity.getProtocolEnum().getProtocolName())
                .append("://")
                .append(serviceEntity.getEndPoint()).toString();
        ByteSequence key = ByteSequence.from(serviceKey.getBytes());
        ServiceCenterValue serviceCenterValue = null;
        try {
            serviceCenterValue = ServiceCenterValue.builder()
                    .endPoint(serviceEntity.getEndPoint())
                    .env(serviceEntity.getEnvEnum().getEnvName()).build();
        } catch (NullPointerException e) {
            System.out.println("error service entity");
            System.out.println(e.getMessage());
        }
        String serviceValue = JSON.toJSONString(serviceCenterValue);
        ByteSequence value = ByteSequence.from(serviceValue.getBytes());
        //租约
        Lease leaseClient = client.getLeaseClient();
        try {
            LeaseGrantResponse leaseGrantResponse = leaseClient.grant(leaseTimeToLive).get(1000, TimeUnit.MILLISECONDS);
            System.out.println(String.format("service-governance success apply lease:%d", leaseGrantResponse.getID()));
            this.leaseId = leaseGrantResponse.getID();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        //注册
        if (this.leaseId > 0) {
            KV kvClient = client.getKVClient();
            kvClient.put(key, value, PutOption.newBuilder().withLeaseId(leaseId).build());
            keepAlive();
        }
    }

    @Override
    public void reRegister() {
        //更新默认leaseID
        this.leaseId = 0L;
        System.out.println("reRegister...");
        register();
    }

    @Override
    public void unRegister() {
        Lease leaseClient = client.getLeaseClient();
        try {
            leaseClient.revoke(this.leaseId).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void keepAlive() {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1,
                new ServiceGovernanceThreadFactory("service-governance-keepalive-thread"));
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                System.out.println(String.format("%s keepalive leaseId:%d", Thread.currentThread().getName(), leaseId));
                try {
                    LeaseKeepAliveResponse leaseKeepAliveResponse = client.getLeaseClient().keepAliveOnce(leaseId).get();
                    if (leaseKeepAliveResponse.getTTL() == 0) {
                        //需要重新注册服务
                        scheduledExecutorService.shutdown();
                        reRegister();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }, 0, KeepAliveIntervalMs, TimeUnit.MILLISECONDS);
    }
}
