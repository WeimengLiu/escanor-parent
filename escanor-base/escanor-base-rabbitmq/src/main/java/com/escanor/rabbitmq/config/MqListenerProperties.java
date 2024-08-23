package com.escanor.rabbitmq.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * @author : Accelerator
 * @date : 2019/7/9 11:24
 */
@Setter
@Getter
@ConfigurationProperties(prefix = "spring.rabbitmq.queue")
public class MqListenerProperties {
    /**
     * 是否开启监听
     */
    private boolean enableListener = false;

    /**
     * message send timeout
     */
    private Integer sendTimeout = 30000;

    private Map<String, MqListener> listeners = new HashMap<>();


    @Setter
    @Getter
    public static class MqListener {

        private String[] queueNames;

        private Integer minConsumer = 5;

        private Integer maxConsumer = 10;

        /**
         * thread max size
         */
        //private int threadMaxSize = 20;


        private Long recoveryInterval = 5000L;

        private Integer maxMessagesPerTask = 20;

        private Long receiveTimeout = 30000L;

        private ListenerHandler listenerHandler = new ListenerHandler(ListenerType.LISTENER,"defaultMqListener");

        private int queuePrefetch = 10;


        /**
         * 线程池队列最大值
         */
        private Integer queueCapacity = 200;

    }

    @Setter
    @Getter
    public static class ListenerHandler {
        private ListenerType type;
        private String beanName;

        private String methodName;

        public ListenerHandler(ListenerType type, String beanName) {
            this.type = type;
            this.beanName = beanName;
        }

        public ListenerHandler(ListenerType type, String beanName, String methodName) {
            this.type = type;
            this.beanName = beanName;
            this.methodName = methodName;
        }

    }

    public enum ListenerType {
        LISTENER,
        METHOD
    }

}
