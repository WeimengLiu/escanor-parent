package com.escanor.rabbitmq.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;

import java.util.ArrayList;
import java.util.List;

public class RabbitAdminConfig {

    private List<Exchange> exchanges;
    private List<Queue> queues;
    private List<Binding> bindings;
    private List<String> customKeys = new ArrayList<>();

    public static RabbitAdminConfigBuilder builder() {
        return new RabbitAdminConfigBuilder();
    }

    public List<Exchange> getExchanges() {
        return exchanges;
    }

    public void setExchanges(List<Exchange> exchanges) {
        this.exchanges = exchanges;
    }

    public List<Queue> getQueues() {
        return queues;
    }

    public void setQueues(List<Queue> queues) {
        this.queues = queues;
    }

    public List<Binding> getBindings() {
        return bindings;
    }

    public void setBindings(List<Binding> bindings) {
        this.bindings = bindings;
    }

    public List<String> getCustomKeys() {
        return customKeys;
    }

    public void setCustomKeys(List<String> customKeys) {
        this.customKeys = customKeys;
    }

    public static final class RabbitAdminConfigBuilder {
        private List<Exchange> exchanges;
        private List<Queue> queues;
        private List<Binding> bindings;
        private List<String> customKeys = new ArrayList<>();

        private RabbitAdminConfigBuilder() {
        }

        public static RabbitAdminConfigBuilder aRabbitAdminConfig() {
            return new RabbitAdminConfigBuilder();
        }

        public RabbitAdminConfigBuilder exchanges(List<Exchange> exchanges) {
            this.exchanges = exchanges;
            return this;
        }

        public RabbitAdminConfigBuilder queues(List<Queue> queues) {
            this.queues = queues;
            return this;
        }

        public RabbitAdminConfigBuilder bindings(List<Binding> bindings) {
            this.bindings = bindings;
            return this;
        }

        public RabbitAdminConfigBuilder customKeys(List<String> customKeys) {
            this.customKeys = customKeys;
            return this;
        }

        public RabbitAdminConfig build() {
            RabbitAdminConfig rabbitAdminConfig = new RabbitAdminConfig();
            rabbitAdminConfig.setExchanges(exchanges);
            rabbitAdminConfig.setQueues(queues);
            rabbitAdminConfig.setBindings(bindings);
            rabbitAdminConfig.setCustomKeys(customKeys);
            return rabbitAdminConfig;
        }
    }
}
