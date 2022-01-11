package com.escanor.rabbitmq.config;

import brave.spring.rabbit.SpringRabbitTracing;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerEndpoint;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpoint;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.Map;

@Configuration
@EnableConfigurationProperties(MyMqProperties.class)
@ConditionalOnExpression("${spring.rabbitmq.queue.enableListener:false}")
public class RabbitMqListenerConfig implements RabbitListenerConfigurer {

    final Log log = LogFactory.getLog(RabbitMqListenerConfig.class);

    private CachingConnectionFactory connectionFactory;
    private MyMqProperties myMqProperties;
    private SpringRabbitTracing springRabbitTracing;
    private MessageConverter messageConverter;

    @Autowired
    private void setMessageConverter(MessageConverter converter) {
        this.messageConverter = converter;
    }

    @Autowired
    private void setConnectionFactory(CachingConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Autowired
    private void setMyMqProperties(MyMqProperties myMqProperties) {
        this.myMqProperties = myMqProperties;
    }

    @Autowired
    private void setSpringRabbitTracing(SpringRabbitTracing springRabbitTracing) {
        this.springRabbitTracing = springRabbitTracing;
    }

    @Override
    public void configureRabbitListeners(RabbitListenerEndpointRegistrar registrar) {
        log.info("初始化RabbitMq Listeners");
        try {
            initConsumerListeners(registrar);
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
                String queueName = entry.getKey();
                MyMqProperties.MyMqListener listenerProperties = entry.getValue();
                RabbitListenerEndpoint endpoint = getRabbitMqListener(queueName, listenerProperties);
                SimpleRabbitListenerContainerFactory factory = createRabbitMqListenerContainerFactory(queueName, listenerProperties);
                factory.setConnectionFactory(connectionFactory);
                registrar.registerEndpoint(endpoint, factory);
            }
        }
    }

    private RabbitListenerEndpoint getRabbitMqListener(String queueName, MyMqProperties.MyMqListener listenerProperties) throws ClassNotFoundException {
        SimpleRabbitListenerEndpoint endpoint = new SimpleRabbitListenerEndpoint();
        Assert.isTrue(listenerProperties.getMinConsumer() <= listenerProperties.getMaxConsumer(), "最大消费者必须大于或等于大小消费者");
        String consumers = listenerProperties.getMinConsumer() + "-" + listenerProperties.getMaxConsumer();
        endpoint.setId(queueName);
        endpoint.setQueueNames(listenerProperties.getQueueNames());
        endpoint.setConcurrency(consumers);
        MessageListener listener = (MessageListener) BeanUtils.instantiateClass(Class.forName(listenerProperties.getListenerName()));
        //listener.setHandlerBeanName(listenerProperties.getHandlerName());
        endpoint.setMessageListener(listener);
        endpoint.setMessageConverter(messageConverter);
        return endpoint;
    }
}
