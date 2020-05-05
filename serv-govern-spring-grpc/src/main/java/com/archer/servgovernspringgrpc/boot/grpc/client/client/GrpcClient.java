package com.archer.servgovernspringgrpc.boot.grpc.client.client;

import io.grpc.*;
import io.grpc.stub.AbstractStub;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface GrpcClient {

    /**
     * clientName
     *
     * @return
     */
    String value();
}
