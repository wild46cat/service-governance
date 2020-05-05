package com.archer.servgovernspringgrpc.governance.etcd;

import io.etcd.jetcd.Client;
import org.apache.commons.lang3.StringUtils;

/**
 * singleton etcdclient
 */
public class EtcdClient {
    private static volatile EtcdClient INSTANCE = null;

    public EtcdClient(String etcdEndpoints) {
        if (StringUtils.isNotBlank(etcdEndpoints)) {
            String[] strings = etcdEndpoints.split(",");
            client = Client.builder().endpoints(strings).build();
        }
    }

    private Client client;

    public static Client getInstance(String etcdEndpoints) {
        if (INSTANCE == null) {
            synchronized (EtcdClient.class) {
                if (INSTANCE == null) {
                    INSTANCE = new EtcdClient(etcdEndpoints);
                }
            }
        }
        return INSTANCE.client;
    }

}
