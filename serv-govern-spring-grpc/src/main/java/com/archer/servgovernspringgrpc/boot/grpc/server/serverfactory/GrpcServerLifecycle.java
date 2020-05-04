package com.archer.servgovernspringgrpc.boot.grpc.server.serverfactory;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import com.archer.servgovernspringgrpc.governance.ServiceGovernanceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;

import io.grpc.Server;

public class GrpcServerLifecycle implements SmartLifecycle {
    private static final Logger log = LoggerFactory.getLogger(GrpcServerLifecycle.class);
    private static AtomicInteger serverCounter = new AtomicInteger(-1);

    private volatile Server server;
    private volatile int phase = Integer.MAX_VALUE;
    private final GrpcServerFactory factory;
    /**
     * 处理服务治理相关
     */
    private final ServiceGovernanceClient serviceGovernanceClient;

    public GrpcServerLifecycle(final GrpcServerFactory factory, ServiceGovernanceClient serviceGovernanceClient) {
        this.factory = factory;
        this.serviceGovernanceClient = serviceGovernanceClient;
    }

    @Override
    public void start() {
        try {
            createAndStartGrpcServer();
        } catch (final IOException e) {
            throw new IllegalStateException("Failed to start the grpc server", e);
        }
    }

    @Override
    public void stop() {
        stopAndReleaseGrpcServer();
    }

    @Override
    public void stop(final Runnable callback) {
        stop();
        callback.run();
    }

    @Override
    public boolean isRunning() {
        return this.server != null && !this.server.isShutdown();
    }

    @Override
    public int getPhase() {
        return this.phase;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    /**
     * Creates and starts the grpc server.
     *
     * @throws IOException If the server is unable to bind the port.
     */
    protected void createAndStartGrpcServer() throws IOException {
        final Server localServer = this.server;
        if (localServer == null) {
            this.server = this.factory.createServer();
            if (this.server.getServices().size() == 0) {
                log.info("gRPC no GrpcService was founded!");
                return;
            }
            this.server.start();
            log.info("gRPC Server started, listening on address: " + this.factory.getAddress() + ", port: "
                    + this.factory.getPort());
            serviceGovernanceClient.start();

            // Prevent the JVM from shutting down while the server is running
            final Thread awaitThread = new Thread(() -> {
                try {
                    this.server.awaitTermination();
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "grpc-server-container-" + (serverCounter.incrementAndGet()));
            awaitThread.setDaemon(false);
            awaitThread.start();
        }
    }

    /**
     * Initiates an orderly shutdown of the grpc server and releases the references to the server. This call does not
     * wait for the server to be completely shut down.
     */
    protected void stopAndReleaseGrpcServer() {
        final Server localServer = this.server;
        if (localServer != null) {
            localServer.shutdown();
            this.server = null;
            log.info("gRPC server shutdown.");
        }
    }

}
