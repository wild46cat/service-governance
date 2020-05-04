package com.archer.servgovernspringgrpc.boot.grpc.server.server;

import com.google.api.client.util.Lists;
import io.grpc.BindableService;
import io.grpc.ServerServiceDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class AnnotationGrpcServiceDiscoverer implements ApplicationContextAware, GrpcServiceDiscoverer {
    private Logger log = LoggerFactory.getLogger(AnnotationGrpcServiceDiscoverer.class);


    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


    @Override
    public List<GrpcServiceDefinition> findGrpcServices() {
        List<String> beanNames =
                Arrays.asList(this.applicationContext.getBeanNamesForAnnotation(GrpcService.class));
        List<GrpcServiceDefinition> definitions = Lists.newArrayListWithCapacity(beanNames.size());
        for (String beanName : beanNames) {
            BindableService bindableService = this.applicationContext.getBean(beanName, BindableService.class);
            ServerServiceDefinition serviceDefinition = bindableService.bindService();
            GrpcService grpcServiceAnnotation = applicationContext.findAnnotationOnBean(beanName, GrpcService.class);
            definitions.add(new GrpcServiceDefinition(beanName, bindableService.getClass(), serviceDefinition));
            log.debug("Found gRPC service: " + serviceDefinition.getServiceDescriptor().getName() + ", bean: "
                    + beanName + ", class: " + bindableService.getClass().getName());
        }
        return definitions;
    }

}
