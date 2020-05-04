package com.archer.servgovernspringgrpc.boot.grpc.server.serverfactory;

import java.util.Objects;
import java.util.function.Consumer;

import io.grpc.ServerBuilder;

/**
 * A configurer for {@link ServerBuilder}s which can be used by {@link GrpcServerFactory} to customize the created
 * server.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@FunctionalInterface
public interface GrpcServerConfigurer extends Consumer<ServerBuilder<?>> {

    @Override
    default GrpcServerConfigurer andThen(final Consumer<? super ServerBuilder<?>> after) {
        Objects.requireNonNull(after);
        return t -> {
            accept(t);
            after.accept(t);
        };
    }

}
