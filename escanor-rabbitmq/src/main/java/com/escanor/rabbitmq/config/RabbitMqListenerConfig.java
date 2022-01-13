package com.escanor.rabbitmq.config;

import brave.spring.rabbit.SpringRabbitTracing;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerEndpoint;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.listener.AbstractRabbitListenerEndpoint;
import org.springframework.amqp.rabbit.listener.MethodRabbitListenerEndpoint;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpoint;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.messaging.handler.annotation.support.MessageHandlerMethodFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.*;

@Configuration
@EnableConfigurationProperties(MyMqProperties.class)
@ConditionalOnExpression("${spring.rabbitmq.queue.enableListener:false}")
public class RabbitMqListenerConfig implements RabbitListenerConfigurer {

    final String LISTENER_TYPE = "listener";
    final String METHOD_TYPE = "method";

    final Log log = LogFactory.getLog(RabbitMqListenerConfig.class);

    private final CachingConnectionFactory connectionFactory;
    private final MyMqProperties myMqProperties;
    private final SpringRabbitTracing springRabbitTracing;
    private final MessageConverter messageConverter;
    private final BeanFactory beanFactory;
    private final MessageHandlerMethodFactory messageHandlerMethodFactory;
    private final Set<String> queueNames = new HashSet<>();

    public RabbitMqListenerConfig(CachingConnectionFactory connectionFactory, MyMqProperties myMqProperties, SpringRabbitTracing springRabbitTracing, MessageConverter converter, BeanFactory beanFactory) {
        this.connectionFactory = connectionFactory;
        this.myMqProperties = myMqProperties;
        this.springRabbitTracing = springRabbitTracing;
        messageConverter = converter;
        this.beanFactory = beanFactory;
        this.messageHandlerMethodFactory = createDefaultMessageHandlerMethodFactory();
    }


    @Override
    public void configureRabbitListeners(RabbitListenerEndpointRegistrar registrar) {
        log.info("初始化RabbitMq Listeners");
        try {
            initConsumerListeners(registrar);
            this.queueNames.forEach(queueName -> {
                ConfigurableBeanFactory configurableBeanFactory = (ConfigurableBeanFactory)this.beanFactory;
                String beanName = "queue." + queueName;
                if (!configurableBeanFactory.containsBean(beanName)) {
                    configurableBeanFactory.registerSingleton("queue." + queueName, new Queue(queueName, true));
                }
            });
        } catch (ClassNotFoundException e) {
            log.error(e.getMessage(), e);
            log.error("初始化RabbitMq Listener异常");
            throw new RuntimeException(e);
        }
        log.info("初始化RabbitMq Listeners结束");
    }


    private SimpleRabbitListenerContainerFactory createRabbitMqListenerContainerFactory(String queueName, MyMqProperties.MyMqListener listenerProperties) {
        SimpleRabbitListenerContainerFactory factory = springRabbitTracing.newSimpleRabbitListenerContainerFactory(connectionFactory);
        // 重连间隔时间
        factory.setRecoveryInterval(listenerProperties.getRecoveryInterval());
        //设置接受消息超时时间
        factory.setReceiveTimeout(listenerProperties.getReceiveTimeout());
        factory.setConsumerBatchEnabled(false);
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setMaxPoolSize(listenerProperties.getThreadMaxSize());
        if (listenerProperties.getMaxConsumer() > listenerProperties.getThreadMaxSize()) {
            taskExecutor.setMaxPoolSize(listenerProperties.getMaxConsumer() + 1);
        }
        taskExecutor.setCorePoolSize(listenerProperties.getMaxConsumer());
        taskExecutor.setQueueCapacity(listenerProperties.getQueueCapacity());
        taskExecutor.setThreadNamePrefix(queueName + "-");
        taskExecutor.initialize();
        factory.setTaskExecutor(taskExecutor);
        factory.setConcurrentConsumers(listenerProperties.getMinConsumer());
        factory.setPrefetchCount(listenerProperties.getQueuePrefetch());
        factory.setMaxConcurrentConsumers(listenerProperties.getMaxConsumer());
        //开启事务
        factory.setChannelTransacted(true);
        return factory;
    }

    private void initConsumerListeners(RabbitListenerEndpointRegistrar registrar) throws ClassNotFoundException {
        Map<String, MyMqProperties.MyMqListener> listeners = myMqProperties.getListeners();
        if (!CollectionUtils.isEmpty(listeners)) {
            for (Map.Entry<String, MyMqProperties.MyMqListener> entry : listeners.entrySet()) {
                String id = entry.getKey();
                MyMqProperties.MyMqListener listenerProperties = entry.getValue();
                RabbitListenerEndpoint endpoint = getRabbitMqListener(id, listenerProperties);
                SimpleRabbitListenerContainerFactory factory = createRabbitMqListenerContainerFactory(id, listenerProperties);
                factory.setConnectionFactory(connectionFactory);
                registrar.registerEndpoint(endpoint, factory);
            }
        }
    }

    private RabbitListenerEndpoint getRabbitMqListener(String id, MyMqProperties.MyMqListener listenerProperties) throws ClassNotFoundException {
        String[] listenerParams = StringUtils.split(listenerProperties.getListenerHandler(), ":");
        String type = listenerParams[0];
        AbstractRabbitListenerEndpoint endpoint = null;
        if (LISTENER_TYPE.equalsIgnoreCase(type)) {
             endpoint = createSimpleRabbitListenerEndpoint(listenerProperties, listenerParams);
        } else if (METHOD_TYPE.equalsIgnoreCase(type)) {
            endpoint = createMethodRabbitListenerEndpoint(listenerProperties, listenerParams);
        } else {
            throw new IllegalArgumentException(type + ": unsupported listener type, please check your configuration, supported type: listener , method");
        }
        Assert.isTrue(listenerProperties.getMinConsumer() <= listenerProperties.getMaxConsumer(), "最大消费者必须大于或等于大小消费者");
        String consumers = listenerProperties.getMinConsumer() + "-" + listenerProperties.getMaxConsumer();
        endpoint.setId(id);
        endpoint.setQueueNames(listenerProperties.getQueueNames());
        this.queueNames.addAll(Arrays.asList(listenerProperties.getQueueNames()));
        endpoint.setConcurrency(consumers);
        endpoint.setMessageConverter(messageConverter);
        return endpoint;

    }

    private SimpleRabbitListenerEndpoint createSimpleRabbitListenerEndpoint(MyMqProperties.MyMqListener listenerProperties, String[] listenerParams) {
        Assert.isTrue(listenerParams.length == 2, "listenerHandler configuration error");
        String listenerName = listenerParams[1];
        SimpleRabbitListenerEndpoint endpoint = new SimpleRabbitListenerEndpoint();
        MessageListener listener = (MessageListener) beanFactory.getBean(listenerName);
        endpoint.setMessageListener(listener);
        return endpoint;
    }

    private MethodRabbitListenerEndpoint createMethodRabbitListenerEndpoint(MyMqProperties.MyMqListener listenerProperties, String[] listenerParams) {
        Assert.isTrue(listenerParams.length == 3, "listenerHandler configuration error");
        String listenerName = listenerParams[1];
        String methodName = listenerParams[2];
        MethodRabbitListenerEndpoint methodRabbitListenerEndpoint = new MethodRabbitListenerEndpoint();
        Object bean = beanFactory.getBean(listenerName);
        Method method = ReflectionUtils.findMethod(AopUtils.getTargetClass(bean), methodName, null);
        methodRabbitListenerEndpoint.setBean(bean);
        Assert.notNull(method, "not find method " + methodName + " in bean " + listenerName);
        methodRabbitListenerEndpoint.setMethod(method);
        methodRabbitListenerEndpoint.setMessageHandlerMethodFactory(this.messageHandlerMethodFactory);
        return methodRabbitListenerEndpoint;
    }

    private MessageHandlerMethodFactory createDefaultMessageHandlerMethodFactory() {
        DefaultMessageHandlerMethodFactory defaultFactory = new DefaultMessageHandlerMethodFactory();
        defaultFactory.setBeanFactory(this.beanFactory);
        defaultFactory.afterPropertiesSet();
        return defaultFactory;
    }
}
