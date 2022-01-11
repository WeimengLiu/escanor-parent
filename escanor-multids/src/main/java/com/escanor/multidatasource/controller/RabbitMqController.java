package com.escanor.multidatasource.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
public class RabbitMqController {

    final Log log = LogFactory.getLog(RabbitMqController.class);

    final RabbitTemplate rabbitTemplate;

    public RabbitMqController(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @RequestMapping(value = "/sendMessage/{key}", method = RequestMethod.POST)
    public void sendMessage(@PathVariable(name = "key") String key, @RequestBody String message) {
        log.info("消息路由KEY：" + key + "，消息: [" +  message +"]");
        rabbitTemplate.convertAndSend(key, message);
    }
}
