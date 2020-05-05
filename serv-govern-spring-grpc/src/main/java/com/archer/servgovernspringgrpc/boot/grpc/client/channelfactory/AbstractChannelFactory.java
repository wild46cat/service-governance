package com.archer.servgovernspringgrpc.boot.grpc.client.channelfactory;

import com.archer.servgovernspringgrpc.governance.config.GovernanceConfig;
import com.archer.servgovernspringgrpc.governance.etcd.EtcdClient;
import com.archer.servgovernspringgrpc.governance.resolver.EtcdNameResolverFactory;
import io.etcd.jetcd.Client;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jdk.jfr.events.SocketReadEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import javax.annotation.concurrent.GuardedBy;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

public abstract class AbstractChannelFactory<T extends ManagedChannelBuilder<T>> implements GrpcChannelFactory {
    private static final Logger log = LoggerFactory.getLogger(AbstractChannelFactory.class);
    private GovernanceConfig governanceConfig;

    @GuardedBy("this")
    private final Map<String, ManagedChannel> channels = new ConcurrentHashMap<>();
    private boolean shutdown = false;

    public AbstractChannelFactory(GovernanceConfig governanceConfig) {
        this.governanceConfig = governanceConfig;
    }

    @Override
    public Channel createChannel(final String name) {
        final Channel channel;
        synchronized (this) {
            if (this.shutdown) {
                throw new IllegalStateException("GrpcChannelFactory is already closed!");
            }
            channel = this.channels.computeIfAbsent(name, this::newManagedChannel);
        }
        return channel;
    }

    protected abstract T newChannelBuilder(String name);

    /**
     * 新建channel
     *
     * @param name
     * @return
     */
    protected ManagedChannel newManagedChannel(final String name) {
        String etcdEndPoints = governanceConfig.getEtcdEndpoints();
        Client client = EtcdClient.getInstance(etcdEndPoints);
        EtcdNameResolverFactory etcdNameResolverFactory = null;
        if (governanceConfig.isClientDebugFlag()) {
            //debug模式，客户端指定server地址
            Map<String, List<String>> clientDebugInfo = governanceConfig.getClientDebugInfo();
            List<String> fixedAddressList = clientDebugInfo.get(name);
            List<SocketAddress> fixedSockerAddress = new ArrayList<>();
            if (fixedAddressList != null && fixedAddressList.size() > 0) {
                for (String fixedAddress : fixedAddressList) {
                    log.info(String.format("add fixed address for service:%s,endpoint:%s", name, fixedAddress));
                    String[] split = fixedAddress.split(":");
                    fixedSockerAddress.add(new InetSocketAddress(split[0], Integer.valueOf(split[1])));
                }
            }
            SocketAddress[] socketAddresseArray = new SocketAddress[fixedSockerAddress.size()];
            fixedSockerAddress.toArray(socketAddresseArray);
            etcdNameResolverFactory = new EtcdNameResolverFactory(client, socketAddresseArray);
        } else {
            etcdNameResolverFactory = new EtcdNameResolverFactory(client);
        }

        final T builder = newChannelBuilder(name);
        final ManagedChannel channel = builder.nameResolverFactory(etcdNameResolverFactory)
                .defaultLoadBalancingPolicy("round_robin")
                .usePlaintext().build();
        return channel;
    }


    /**
     * Closes this channel factory and the channels created by this instance. The shutdown happens in two phases, first
     * an orderly shutdown is initiated on all channels and then the method waits for all channels to terminate. If the
     * channels don't have terminated after 60 seconds then they will be forcefully shutdown.
     */
    @Override
    @PreDestroy
    public synchronized void close() {
        if (this.shutdown) {
            return;
        }
        this.shutdown = true;
        for (final ManagedChannel channel : this.channels.values()) {
            channel.shutdown();
        }
        try {
            final long waitLimit = System.currentTimeMillis() + 60_000; // wait 60 seconds at max
            for (final ManagedChannel channel : this.channels.values()) {
                int i = 0;
                do {
                    log.debug("Awaiting channel shutdown: {} ({}s)", channel, i++);
                } while (System.currentTimeMillis() < waitLimit && !channel.awaitTermination(1, TimeUnit.SECONDS));
            }
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            log.debug("We got interrupted - Speeding up shutdown process");
        } finally {
            for (final ManagedChannel channel : this.channels.values()) {
                if (!channel.isTerminated()) {
                    log.debug("Channel not terminated yet - force shutdown now: {} ", channel);
                    channel.shutdownNow();
                }
            }
        }
        final int channelCount = this.channels.size();
        this.channels.clear();
        log.debug("GrpcCannelFactory closed (including {} channels)", channelCount);
    }

}
