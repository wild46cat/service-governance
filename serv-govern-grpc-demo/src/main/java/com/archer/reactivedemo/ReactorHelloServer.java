package com.archer.reactivedemo;

import com.archer.demo.helloworld.HelloReply;
import com.archer.demo.helloworld.HelloRequest;
import com.archer.demo.helloworld.ReactorGreeterGrpc;
import com.archer.service_governance.enums.EnvEnum;
import com.archer.service_governance.enums.ProtocolEnum;
import com.archer.service_governance.beans.ServiceEntity;
import com.archer.service_governance.ServiceGovernanceClient;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import reactor.core.publisher.Mono;

import java.io.IOException;

public class ReactorHelloServer {
    public static final int PORT = 50051;
    private Server server2;

    public static void main(String[] args) throws IOException, InterruptedException {
        final ReactorHelloServer reactorHelloServer = new ReactorHelloServer();
        reactorHelloServer.start();
        //注册服务
        ServiceEntity serviceEntity = ServiceEntity.builder()
                .serviceName("helloworld")
                .endPoint("localhost:50051")
                .protocolEnum(ProtocolEnum.gRPC)
                .envEnum(EnvEnum.DEV).build();
        ServiceGovernanceClient governanceClient = new ServiceGovernanceClient("", serviceEntity);
        reactorHelloServer.blockUntilShutdown();
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server2 != null) {
            server2.awaitTermination();
        }
    }

    public void start() throws IOException {
        server2 = ServerBuilder.forPort(PORT)
                .addService(new ReactorHelloServer.ReactorGreeterImpl())
                .build()
                .start();
        System.out.println("start " + PORT + "...");
    }

    static class ReactorGreeterImpl extends ReactorGreeterGrpc.GreeterImplBase {
        @Override
        public Mono<HelloReply> sayHello(Mono<HelloRequest> request) {
            Mono<HelloReply> res = request.map(req -> {
                System.out.println("receive:" + req.getName());
                HelloReply reply = HelloReply.newBuilder().setMessage("Hello " + req.getName() + "  port:" + PORT).build();
                return reply;
            });
            return res;
        }
    }
}
