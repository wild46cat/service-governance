package com.archer.service_governance;

import io.etcd.jetcd.Client;
import io.grpc.EquivalentAddressGroup;
import io.grpc.NameResolver;

import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EtcdNameResolverFactory extends NameResolver.Factory {
    /**
     * 默认地址，指定后为固定值，不再通过配置中心动态获取
     */
    List<EquivalentAddressGroup> addresses = new ArrayList<>();
    private Client client;

    public EtcdNameResolverFactory(Client client, SocketAddress... defaultAddress) {
        this.addresses = Arrays.stream(defaultAddress)
                .map(EquivalentAddressGroup::new)
                .collect(Collectors.toList());
        this.client = client;
    }

    public NameResolver newNameResolver(URI serverServiceURI, NameResolver.Args args) {
        if (addresses.size() > 0) {
            return new DefaultNameResolver(addresses);
        } else {
            return new EtcdNameResolver(client, serverServiceURI.getRawPath());
        }
    }

    @Override
    public String getDefaultScheme() {
        return "grpc";
    }
}
