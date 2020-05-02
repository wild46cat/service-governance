package com.archer.demo;

import com.archer.demo.helloworld.GreeterGrpc;
import com.archer.demo.helloworld.HelloReply;
import com.archer.demo.helloworld.HelloRequest;
import com.archer.service_governance.EnvEnum;
import com.archer.service_governance.ProtocolEnum;
import com.archer.service_governance.ServiceEntity;
import com.archer.service_governance.ServiceGovernanceClient;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;

public class HelloServer2 {
    private static final int PORT = 50052;

    private Server server2;

    public static void main(String[] args) throws IOException, InterruptedException {
        final HelloServer2 helloServererver = new HelloServer2();
        helloServererver.start();
        //注册服务
        ServiceEntity serviceEntity = ServiceEntity.builder()
                .serviceName("helloworld")
                .endPoint("localhost:50052")
                .protocolEnum(ProtocolEnum.gRPC)
                .envEnum(EnvEnum.DEV).build();
        ServiceGovernanceClient governanceClient = new ServiceGovernanceClient("", serviceEntity);
        helloServererver.blockUntilShutdown();
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
                .addService(new GreeterImpl())
                .build()
                .start();
        System.out.println("start " + PORT + "...");
    }

    static class GreeterImpl extends GreeterGrpc.GreeterImplBase {

        @Override
        public void sayHello(HelloRequest req, StreamObserver<HelloReply> responseObserver) {
            HelloReply reply = HelloReply.newBuilder().setMessage("Hello " + req.getName() + "  port:" + PORT).build();
            System.out.println("receive:" + req.getName());
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }
}
