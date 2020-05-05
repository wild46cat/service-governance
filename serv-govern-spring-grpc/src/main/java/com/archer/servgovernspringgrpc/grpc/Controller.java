package com.archer.servgovernspringgrpc.grpc;

import com.archer.demo.helloworld.GreeterGrpc;
import com.archer.demo.helloworld.HelloReply;
import com.archer.demo.helloworld.HelloRequest;
import com.archer.demo.helloworld.ReactorGreeterGrpc;
import com.archer.servgovernspringgrpc.boot.grpc.client.client.GrpcClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/test")
public class Controller {

    @GrpcClient("helloworld")
    private GreeterGrpc.GreeterBlockingStub greeterStub;

    @GrpcClient("helloworld")
    private GreeterGrpc.GreeterFutureStub greeterStub2;

    @GrpcClient("helloworld")
    private ReactorGreeterGrpc.ReactorGreeterStub greeterStub3;

    @RequestMapping("/a")
    public String a() {
        HelloReply reply = greeterStub.sayHello(HelloRequest.newBuilder().setName("archer").build());
        HelloReply reply2;
        try {
            reply2 = greeterStub2.sayHello(HelloRequest.newBuilder().setName("archer").build()).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return reply.getMessage();
    }

    @RequestMapping("/reactora")
    public Mono<HelloReply> reactora() {
        return greeterStub3.sayHello(HelloRequest.newBuilder().setName("archer").build());
    }
}
