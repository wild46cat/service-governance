package com.archer.service_governance;

import io.grpc.Attributes;
import io.grpc.EquivalentAddressGroup;
import io.grpc.NameResolver;

import java.util.List;

public class DefaultNameResolver extends NameResolver {
    List<EquivalentAddressGroup> addressGroups;

    public DefaultNameResolver(List<EquivalentAddressGroup> addressGroups) {
        this.addressGroups = addressGroups;
    }

    @Override
    public void start(Listener listener) {
        listener.onAddresses(addressGroups, Attributes.EMPTY);
    }

    @Override
    public String getServiceAuthority() {
        return "";
    }

    @Override
    public void shutdown() {

    }
}
