package com.escanor.rabbitmq.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Configuration
@EnableConfigurationProperties(CodeQueueMapperProperties.class)
public class RabbitMqConfig implements SmartInitializingSingleton {

    private int increment = 0;

    final Log log = LogFactory.getLog(RabbitMqConfig.class);

    private final RabbitAdminConfig config;
    private final BeanFactory beanFactory;

    public RabbitMqConfig(RabbitAdminConfig config, BeanFactory beanFactory) {
        this.config = config;
        this.beanFactory = beanFactory;
    }

    @Bean
    public MessageConverter messageConverter(ObjectProvider<ObjectMapper> objectMappers) {
        ObjectMapper objectMapper = objectMappers.getIfUnique();
        if (null == objectMapper) {
            return new Jackson2JsonMessageConverter();
        } else {
            return new Jackson2JsonMessageConverter(objectMapper);
        }
    }

    @Bean
    @RefreshScope
    CustomRouteKeyCache customRouteKeyCache(RabbitAdminConfig rabbitAdminConfig) {
        return new CustomRouteKeyCache(rabbitAdminConfig.getCustomKeys());
    }


    @Bean
    @RefreshScope
    public RabbitAdminConfig parse(CodeQueueMapperProperties properties) {
        List<Exchange> exchangeList = new ArrayList<>();
        Set<String> queueNames = new HashSet<>();
        List<Binding> bindings  = new ArrayList<>();
        List<String> customKeys = new ArrayList<>();
        if (!CollectionUtils.isEmpty(properties.getBindings())) {
            for (Map.Entry<String, CodeQueueMapperProperties.CodeQueueMappers> mappersEntry : properties.getBindings().entrySet()) {
                DirectExchange directExchange = new DirectExchange(mappersEntry.getKey());
                CodeQueueMapperProperties.CodeQueueMappers codeQueueMappers = mappersEntry.getValue();
                Binding defaultBinding = new Binding(codeQueueMappers.getDefaultQueue(), Binding.DestinationType.QUEUE, mappersEntry.getKey(),codeQueueMappers.getDefaultKey(), null);
                bindings.add(defaultBinding);
                for (CodeQueueMapperProperties.CodeQueueMapper mapper : codeQueueMappers.getMappers()) {
                    queueNames.add(mapper.getQueue());
                    Binding codeBinding = new Binding(mapper.getQueue(), Binding.DestinationType.QUEUE, mappersEntry.getKey(), mappersEntry.getKey() + mapper.getCode(), null);
                    bindings.add(codeBinding);
                    customKeys.add(mappersEntry.getKey() + mapper.getCode());
                }
                exchangeList.add(directExchange);
            }
        }
        List<Queue> queues = queueNames.stream().map(queueName -> new Queue(queueName, true)).collect(Collectors.toList());
        return RabbitAdminConfig.builder().bindings(bindings).exchanges(exchangeList).customKeys(customKeys).queues(queues).build();
    }

    @Override
    public void afterSingletonsInstantiated() {
        try {
            ConfigurableBeanFactory beanFactory =  (ConfigurableBeanFactory)this.beanFactory;
            log.info("开始动态注入Exchange，Binding，Queue");
            Optional.ofNullable(config.getExchanges()).ifPresent(
                    exchanges -> exchanges.forEach(exchange -> beanFactory.registerSingleton("exchange." + exchange.getName(), exchange))
            );
            Optional.ofNullable(config.getBindings()).ifPresent(
                    bindings -> bindings.forEach(binding -> beanFactory.registerSingleton("binding." + binding.getExchange() + "." + binding.getDestination() + ++this.increment, binding))
            );
            Optional.ofNullable(config.getQueues()).ifPresent(
                    queues -> queues.forEach(queue -> beanFactory.registerSingleton("queue." + queue.getName() , queue))
            );
        } catch (Exception e) {
            log.error("动态注入Exchange，Binding，Queue错误");
            log.error(e.getMessage(), e);
        }
    }
}
