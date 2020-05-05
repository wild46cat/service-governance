package com.archer.servgovernspringgrpc.governance.resolver;

import com.alibaba.fastjson.JSONObject;
import com.archer.servgovernspringgrpc.governance.beans.ServiceCenterValue;
import com.archer.servgovernspringgrpc.governance.enums.ProtocolEnum;
import io.etcd.jetcd.*;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.watch.WatchEvent;
import io.grpc.Attributes;
import io.grpc.EquivalentAddressGroup;
import io.grpc.NameResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.archer.servgovernspringgrpc.governance.register.ServiceRegister.serviceRegisterPrefix;
import static com.archer.servgovernspringgrpc.governance.register.ServiceRegister.spliter;


public class EtcdNameResolver extends NameResolver {
    private static final Logger log = LoggerFactory.getLogger(EtcdNameResolver.class);
    private static final Charset charset = Charset.forName("UTF-8");
    private Listener listener;
    private Client client;
    /**
     * 作为client端需要调用的serviceName列表 eg:helloworld
     */
    private String serverServiceName;
    /**
     * 在服务中心的前缀名称 eg:/services/helloworld/grpc
     */
    private String servicePrefixName;

    public EtcdNameResolver(Client client, String serverServiceName) {
        this.client = client;
        this.serverServiceName = serverServiceName;
        this.servicePrefixName = new StringBuilder(serviceRegisterPrefix)
                .append(spliter)
                .append(serverServiceName)
                .append(spliter)
                .append(ProtocolEnum.gRPC.getProtocolName()).toString();
    }

    @Override
    public String getServiceAuthority() {
        return "";
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void start(Listener listener) {
        log.info("name resolver start ......");
        this.listener = listener;
        initServiceEndpoint();
        watch();
    }

    //初始化endPoint
    private void initServiceEndpoint() {
        List<EquivalentAddressGroup> addressGroups = new ArrayList<>();
        KV kvClient = client.getKVClient();
        ByteSequence key = ByteSequence.from(this.servicePrefixName.getBytes());
        try {
            GetResponse getResponse = kvClient.get(key, GetOption.newBuilder().withPrefix(key).build()).get();
            List<KeyValue> kvs = getResponse.getKvs();
            for (KeyValue kv : kvs) {
                ByteSequence byteSequenceValue = kv.getValue();
                ServiceCenterValue value = (ServiceCenterValue) JSONObject.parseObject(byteSequenceValue.getBytes(), ServiceCenterValue.class);
                String endPoint = value.getEndPoint();
                log.info(String.format("service %s found:%s", this.serverServiceName, endPoint));
                String[] addressArray = endPoint.split(":");
                List<SocketAddress> sockaddrsList = new ArrayList<SocketAddress>();
                sockaddrsList.add(new InetSocketAddress(addressArray[0], Integer.parseInt(addressArray[1])));
                addressGroups.add(new EquivalentAddressGroup(sockaddrsList));
            }
        } catch (InterruptedException e) {
            log.error("initServiceEndpoint error", e);
        } catch (ExecutionException e) {
            log.error("initServiceEndpoint error!", e);
        }
        this.listener.onAddresses(addressGroups, Attributes.EMPTY);
    }

    //update endpoint
    private void updateServiceEndpoint() {
        log.info("updateServiceEndpoint...");
        initServiceEndpoint();
    }

    private void watch() {
        //启动一个额外线程
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                CountDownLatch countDownLatch = new CountDownLatch(1);
                log.info("etcd watch begin...");
                Watch watchClient = client.getWatchClient();
                Watch.Listener watchListener = Watch.listener(watchResponse -> {
                    log.info("etcd something changed........................");
                    List<WatchEvent> events = watchResponse.getEvents();
                    List<String> endPoints = new ArrayList<>();
                    for (WatchEvent event : events) {
                        String changedKey = event.getKeyValue().getKey().toString(charset);
                        String changedValue = event.getKeyValue().getValue().toString(charset);
                        log.info("key:" + changedKey + " value:" + changedValue + "change type:" + event.getEventType().toString());
                        endPoints.add(changedValue);
                    }
                    updateServiceEndpoint();
                });
                ByteSequence key = ByteSequence.from("".getBytes());
                ByteSequence prefix = ByteSequence.from(servicePrefixName.getBytes());
                watchClient.watch(key, WatchOption.newBuilder().withPrefix(prefix).build(), watchListener);
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
