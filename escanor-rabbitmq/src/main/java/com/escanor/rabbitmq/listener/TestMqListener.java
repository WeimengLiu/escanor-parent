package com.escanor.rabbitmq.listener;

import com.escanor.rabbitmq.common.ContextHolder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.util.Assert;

//@Component
//@Lazy
//@RabbitListener(queues = "test")
public class TestMqListener implements MessageListener {
    Log log = LogFactory.getLog(TestMqListener.class);

    /*@RabbitHandler
    public void onMessage(String message){
        log.info("receive message:" + message);
    }*/

    @Override
    public void onMessage(Message message) {
        Assert.isTrue(ContextHolder.isApplicationReady(), "应用未启动，消息无法消费");
        log.info("receive message:" + message);
    }
}
