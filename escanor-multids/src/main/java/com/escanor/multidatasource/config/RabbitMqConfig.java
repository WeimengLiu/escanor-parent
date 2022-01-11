package com.escanor.multidatasource.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Bean
    public Queue testQueue() {
        return new Queue("test", true);
    }

    @Bean
    public DirectExchange testDirectExchange() {
        return new DirectExchange("testDirectExchange", true, false);
    }

    @Bean
    Binding bindingDirect(Queue testQueue, DirectExchange testDirectExchange) {
        return BindingBuilder.bind(testQueue).to(testDirectExchange).with("test");
    }

}
