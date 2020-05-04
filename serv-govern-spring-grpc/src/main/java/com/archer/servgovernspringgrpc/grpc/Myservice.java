package com.archer.servgovernspringgrpc.grpc;

import com.archer.servgovernspringgrpc.boot.grpc.server.server.GrpcService;
import com.archer.demo.helloworld.HelloReply;
import com.archer.demo.helloworld.HelloRequest;
import io.grpc.stub.StreamObserver;

@GrpcService
public class Myservice extends com.archer.demo.helloworld.GreeterGrpc.GreeterImplBase {
    @Override
    public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
        HelloReply reply = HelloReply.newBuilder().setMessage("Hello " + request.getName()).build();
        System.out.println("receive:" + request.getName());
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
}
