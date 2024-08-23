package com.escanor.rabbitmq.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.stereotype.Component;

@Component
public class DefaultMqListener implements MessageListener {

    Log log = LogFactory.getLog(DefaultMqListener.class);

    @Override
    public void onMessage(Message message) {
        log.info("receive message:" + message);
    }
}
