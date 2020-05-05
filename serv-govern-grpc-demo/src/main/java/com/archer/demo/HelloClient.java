package com.archer.demo;

import com.archer.demo.helloworld.GreeterGrpc;
import com.archer.demo.helloworld.HelloReply;
import com.archer.demo.helloworld.HelloRequest;
import com.archer.service_governance.*;
import com.archer.service_governance.beans.ServiceEntity;
import com.archer.service_governance.enums.EnvEnum;
import com.archer.service_governance.enums.ProtocolEnum;
import com.archer.service_governance.resolver.EtcdNameResolverFactory;
import io.grpc.*;

import java.util.concurrent.*;

public class HelloClient {

    public static void main(String[] args) {
        //注册服务
        ServiceEntity serviceEntity = ServiceEntity.builder()
                .serviceName("helloworld-client")
                .endPoint("")
                .protocolEnum(ProtocolEnum.gRPC)
                .envEnum(EnvEnum.DEV).build();

        ServiceGovernanceClient governanceClient = new ServiceGovernanceClient("", serviceEntity);
        governanceClient.start();

        //根据etcd动态解析grpc服务名称
        EtcdNameResolverFactory etcdNameResolverFactory = new EtcdNameResolverFactory(governanceClient.getClient());
        //固定地址解析grpc服务名称
//        EtcdNameResolverFactory etcdNameResolverFactory = new EtcdNameResolverFactory(governanceClient.getClient(),
//                new InetSocketAddress("localhost", 50051));

        ManagedChannel managedChannel = ManagedChannelBuilder
                .forTarget("helloworld")
//                .forAddress("localhost", 50051)
                // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
                // needing certificates.
                .nameResolverFactory(etcdNameResolverFactory)
                .defaultLoadBalancingPolicy("round_robin")
                .usePlaintext()
                .build();

        GreeterGrpc.GreeterFutureStub greeterFutureStub = GreeterGrpc.newFutureStub(managedChannel);
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                HelloReply reply = null;
                try {
                    reply = greeterFutureStub.sayHello(HelloRequest.newBuilder().setName("archer").build())
                            .get(2, TimeUnit.SECONDS);
                    String message = reply.getMessage();
                    System.out.println(message);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (TimeoutException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 1, TimeUnit.SECONDS);


    }
}
