package com.archer.servgovernspringgrpc.boot.grpc.server.server;

import org.springframework.stereotype.Service;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Service
public @interface GrpcService {
}


