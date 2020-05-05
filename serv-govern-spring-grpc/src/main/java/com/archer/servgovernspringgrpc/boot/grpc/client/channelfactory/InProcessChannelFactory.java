package com.archer.servgovernspringgrpc.boot.grpc.client.channelfactory;

import com.archer.servgovernspringgrpc.governance.config.GovernanceConfig;
import io.grpc.inprocess.InProcessChannelBuilder;


public class InProcessChannelFactory extends AbstractChannelFactory<InProcessChannelBuilder> {

    public InProcessChannelFactory(GovernanceConfig governanceConfig) {
        super(governanceConfig);
    }

    @Override
    protected InProcessChannelBuilder newChannelBuilder(final String name) {
        return InProcessChannelBuilder.forName(name);
    }

}
