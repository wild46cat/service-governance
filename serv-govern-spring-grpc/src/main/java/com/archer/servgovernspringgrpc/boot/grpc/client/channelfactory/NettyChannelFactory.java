
package com.archer.servgovernspringgrpc.boot.grpc.client.channelfactory;

import com.archer.servgovernspringgrpc.governance.config.GovernanceConfig;
import io.grpc.netty.NettyChannelBuilder;

public class NettyChannelFactory extends AbstractChannelFactory<NettyChannelBuilder> {

    public NettyChannelFactory(GovernanceConfig governanceConfig) {
        super(governanceConfig);
    }

    @Override
    protected NettyChannelBuilder newChannelBuilder(final String name) {
        return NettyChannelBuilder.forTarget(name);
    }

}
