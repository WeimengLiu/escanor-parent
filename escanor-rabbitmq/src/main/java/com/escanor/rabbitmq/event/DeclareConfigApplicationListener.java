package com.escanor.rabbitmq.event;

import com.escanor.rabbitmq.config.RabbitAdminConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.context.event.ApplicationContextInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

//@Component
public class DeclareConfigApplicationListener implements ApplicationListener<ApplicationContextInitializedEvent> {

    final Log log = LogFactory.getLog(DeclareConfigApplicationListener.class);

    //private final ConfigurableBeanFactory beanFactory;
    private final RabbitAdminConfig config;
    private int increment = 0;

    public DeclareConfigApplicationListener(RabbitAdminConfig config) {
        this.config = config;
    }

    /*@Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        try {
            Optional.ofNullable(config.getExchanges()).ifPresent(
                    exchanges -> exchanges.forEach(this.admin::declareExchange)
            );
            Optional.ofNullable(config.getBindings()).ifPresent(
                    bindings -> bindings.forEach(this.admin::declareBinding)
            );
            Optional.ofNullable(config.getQueues()).ifPresent(
                    queues -> queues.forEach(this.admin::declareQueue)
            );
        } catch (Exception e) {
            log.error("");
        }
    }*/

    @Override
    public void onApplicationEvent(ApplicationContextInitializedEvent event) {
        try {
            ConfigurableListableBeanFactory beanFactory =  event.getApplicationContext().getBeanFactory();
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
