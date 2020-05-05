package com.archer.servgovernspringgrpc.boot.grpc.client.client;

import com.archer.servgovernspringgrpc.boot.grpc.client.channelfactory.GrpcChannelFactory;
import com.archer.servgovernspringgrpc.governance.config.GovernanceConfig;
import io.grpc.Channel;
import io.grpc.stub.AbstractAsyncStub;
import io.grpc.stub.AbstractBlockingStub;
import io.grpc.stub.AbstractFutureStub;
import io.grpc.stub.AbstractStub;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.BeansException;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import static java.util.Objects.requireNonNull;

public class GrpcClientBeanPostProcessor implements BeanPostProcessor {

    private final ApplicationContext applicationContext;

    // Is only retrieved when needed to avoid too early initialization of these components,
    // which could lead to problems with the correct bean setup.
    private GrpcChannelFactory channelFactory = null;

    public GrpcClientBeanPostProcessor(final ApplicationContext applicationContext) {
        this.applicationContext = requireNonNull(applicationContext, "applicationContext");
    }

    @Override
    public Object postProcessBeforeInitialization(final Object bean, final String beanName) throws BeansException {
        Class<?> clazz = bean.getClass();
        do {
            for (final Field field : clazz.getDeclaredFields()) {
                final GrpcClient annotation = AnnotationUtils.findAnnotation(field, GrpcClient.class);
                if (annotation != null) {
                    ReflectionUtils.makeAccessible(field);
                    ReflectionUtils.setField(field, bean, processInjectionPoint(field, field.getType(), annotation));
                }
            }
            for (final Method method : clazz.getDeclaredMethods()) {
                final GrpcClient annotation = AnnotationUtils.findAnnotation(method, GrpcClient.class);
                if (annotation != null) {
                    final Class<?>[] paramTypes = method.getParameterTypes();
                    if (paramTypes.length != 1) {
                        throw new BeanDefinitionStoreException(
                                "Method " + method + " doesn't have exactly one parameter.");
                    }
                    ReflectionUtils.makeAccessible(method);
                    ReflectionUtils.invokeMethod(method, bean,
                            processInjectionPoint(method, paramTypes[0], annotation));
                }
            }
            clazz = clazz.getSuperclass();
        } while (clazz != null);
        return bean;
    }

    /**
     * Processes the given injection point and computes the appropriate value for the injection.
     *
     * @param <T>             The type of the value to be injected.
     * @param injectionTarget The target of the injection.
     * @param injectionType   The class that will be used to compute injection.
     * @param annotation      The annotation on the target with the metadata for the injection.
     * @return The value to be injected for the given injection point.
     */
    protected <T> T processInjectionPoint(final Member injectionTarget, final Class<T> injectionType,
                                          final GrpcClient annotation) {
        final String name = annotation.value();
        final Channel channel;
        try {
            channel = getChannelFactory().createChannel(name);
            if (channel == null) {
                throw new IllegalStateException("Channel factory created a null channel for " + name);
            }
        } catch (final RuntimeException e) {
            throw new IllegalStateException("Failed to create channel: " + name, e);
        }

        final T value = valueForMember(name, injectionTarget, injectionType, channel);
        if (value == null) {
            throw new IllegalStateException(
                    "Injection value is null unexpectedly for " + name + " at " + injectionTarget);
        }
        return value;
    }

    private GrpcChannelFactory getChannelFactory() {
        if (this.channelFactory == null) {
            final GrpcChannelFactory factory = this.applicationContext.getBean(GrpcChannelFactory.class);
            this.channelFactory = factory;
            return factory;
        }
        return this.channelFactory;
    }


    /**
     * Creates the instance to be injected for the given member.
     *
     * @param <T>             The type of the instance to be injected.
     * @param name            The name that was used to create the channel.
     * @param injectionTarget The target member for the injection.
     * @param injectionType   The class that should injected.
     * @param channel         The channel that should be used to create the instance.
     * @return The value that matches the type of the given field.
     * @throws BeansException If the value of the field could not be created or the type of the field is unsupported.
     */
    protected <T> T valueForMember(final String name, final Member injectionTarget,
                                   final Class<T> injectionType,
                                   final Channel channel) throws BeansException {
        if (Channel.class.equals(injectionType)) {
            return injectionType.cast(channel);
        } else if (AbstractStub.class.isAssignableFrom(injectionType)) {

            @SuppressWarnings("unchecked") // Eclipse incorrectly marks this as not required
                    AbstractStub<?> stub = createStub(injectionType.asSubclass(AbstractStub.class), channel);
            return injectionType.cast(stub);
        } else {
            throw new InvalidPropertyException(injectionTarget.getDeclaringClass(), injectionTarget.getName(),
                    "Unsupported type " + injectionType.getName());
        }
    }

    /**
     * Creates a stub of the given type.
     *
     * @param <T>      The type of the instance to be injected.
     * @param stubType The type of the stub to create.
     * @param channel  The channel used to create the stub.
     * @return The newly created stub.
     * @throws BeanInstantiationException If the stub couldn't be created.
     */
    protected <T extends AbstractStub<T>> T createStub(final Class<T> stubType, final Channel channel) {
        try {
            // First try the public static factory method
            final String methodName = deriveStubFactoryMethodName(stubType);
            final Class<?> enclosingClass = stubType.getEnclosingClass();
            final Method factoryMethod = enclosingClass.getMethod(methodName, Channel.class);
            return stubType.cast(factoryMethod.invoke(null, channel));
        } catch (final Exception e) {
            try {
                // Use the private constructor as backup
                final Constructor<T> constructor = stubType.getDeclaredConstructor(Channel.class);
                constructor.setAccessible(true);
                return constructor.newInstance(channel);
            } catch (final Exception e1) {
                e.addSuppressed(e1);
            }
            throw new BeanInstantiationException(stubType, "Failed to create gRPC client", e);
        }
    }

    /**
     * Derives the name of the factory method from the given stub type.
     *
     * @param stubType The type of the stub to get it for.
     * @return The name of the factory method.
     * @throws IllegalArgumentException If the method was called with an unsupported stub type.
     */
    protected String deriveStubFactoryMethodName(final Class<? extends AbstractStub<?>> stubType) {
        if (AbstractAsyncStub.class.isAssignableFrom(stubType)) {
            return "newStub";
        } else if (AbstractBlockingStub.class.isAssignableFrom(stubType)) {
            return "newBlockingStub";
        } else if (AbstractFutureStub.class.isAssignableFrom(stubType)) {
            return "newFutureStub";
        } else {
            throw new IllegalArgumentException(
                    "Unsupported stub type: " + stubType.getName() + " -> Please report this issue.");
        }
    }

}
