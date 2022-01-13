package com.escanor.rabbitmq.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * @author : Accelerator
 * @date : 2019/7/9 11:24
 */
@ConfigurationProperties(prefix = "spring.rabbitmq.queue")
public class MyMqProperties {
    /**
     * 是否开启监听
     */
    private boolean enableListener = false;

    /**
     * message send timeout
     */
    private Integer sendTimeout = 30000;

    private Map<String, MyMqListener> listeners = new HashMap<>();


    public boolean isEnableListener() {
        return enableListener;
    }

    public void setEnableListener(boolean enableListener) {
        this.enableListener = enableListener;
    }

    public Integer getSendTimeout() {
        return sendTimeout;
    }

    public void setSendTimeout(Integer sendTimeout) {
        this.sendTimeout = sendTimeout;
    }

    public Map<String, MyMqListener> getListeners() {
        return listeners;
    }

    public void setListeners(Map<String, MyMqListener> listeners) {
        this.listeners = listeners;
    }

    public static class MyMqListener {

        private String[] queueNames;

        private Integer minConsumer = 5;

        private Integer maxConsumer = 10;

        /**
         * thread max size
         */
        private int threadMaxSize = 20;


        private Long recoveryInterval = 5000L;

        private Integer maxMessagesPerTask = 20;

        private Long receiveTimeout = 30000L;

        private String listenerHandler = "listener:defaultMqListener";

        private int queuePrefetch = 10;


        /**
         * 线程池队列最大值
         */
        private Integer queueCapacity = 200;

        public String[] getQueueNames() {
            return queueNames;
        }

        public void setQueueNames(String[] queueNames) {
            this.queueNames = queueNames;
        }

        public Integer getMinConsumer() {
            return minConsumer;
        }

        public void setMinConsumer(Integer minConsumer) {
            this.minConsumer = minConsumer;
        }

        public Integer getMaxConsumer() {
            return maxConsumer;
        }

        public void setMaxConsumer(Integer maxConsumer) {
            this.maxConsumer = maxConsumer;
        }

        public int getThreadMaxSize() {
            return threadMaxSize;
        }

        public void setThreadMaxSize(int threadMaxSize) {
            this.threadMaxSize = threadMaxSize;
        }

        public String getListenerHandler() {
            return listenerHandler;
        }

        public void setListenerHandler(String listenerHandler) {
            this.listenerHandler = listenerHandler;
        }

        public Integer getMaxMessagesPerTask() {
            return maxMessagesPerTask;
        }

        public void setMaxMessagesPerTask(Integer maxMessagesPerTask) {
            this.maxMessagesPerTask = maxMessagesPerTask;
        }

        public Long getReceiveTimeout() {
            return receiveTimeout;
        }

        public void setReceiveTimeout(Long receiveTimeout) {
            this.receiveTimeout = receiveTimeout;
        }

        public Long getRecoveryInterval() {
            return recoveryInterval;
        }

        public void setRecoveryInterval(Long recoveryInterval) {
            this.recoveryInterval = recoveryInterval;
        }

        public Integer getQueueCapacity() {
            return queueCapacity;
        }

        public void setQueueCapacity(Integer queueCapacity) {
            this.queueCapacity = queueCapacity;
        }

        public int getQueuePrefetch() {
            return queuePrefetch;
        }

        public void setQueuePrefetch(int queuePrefetch) {
            this.queuePrefetch = queuePrefetch;
        }
    }

}
