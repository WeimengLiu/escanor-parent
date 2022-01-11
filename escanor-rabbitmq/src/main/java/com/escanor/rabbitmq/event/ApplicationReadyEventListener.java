package com.escanor.rabbitmq.event;

import com.escanor.rabbitmq.common.ContextHolder;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class ApplicationReadyEventListener implements ApplicationListener<ApplicationPreparedEvent> {

    @Override
    public void onApplicationEvent(ApplicationPreparedEvent event) {
        ContextHolder.setApplicationReady();
        //event.getApplicationContext().getBean(TestMqListener.class);
    }
}
