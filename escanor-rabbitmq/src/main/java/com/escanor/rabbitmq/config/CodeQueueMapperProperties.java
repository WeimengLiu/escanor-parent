package com.escanor.rabbitmq.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "spring.rabbitmq")
public class CodeQueueMapperProperties {

    private Map<String, CodeQueueMappers> bindings = new HashMap<>();

    public Map<String, CodeQueueMappers> getBindings() {
        return bindings;
    }

    public void setBindings(Map<String, CodeQueueMappers> bindings) {
        this.bindings = bindings;
    }

    public static class CodeQueueMappers {

        private String defaultKey;
        private String defaultQueue;

        private List<CodeQueueMapper> mappers = new ArrayList<>();

        public List<CodeQueueMapper> getMappers() {
            return mappers;
        }

        public void setMappers(List<CodeQueueMapper> mappers) {
            this.mappers = mappers;
        }

        public String getDefaultKey() {
            return defaultKey;
        }

        public void setDefaultKey(String defaultKey) {
            this.defaultKey = defaultKey;
        }

        public String getDefaultQueue() {
            return defaultQueue;
        }

        public void setDefaultQueue(String defaultQueue) {
            this.defaultQueue = defaultQueue;
        }
    }

    public static class CodeQueueMapper {
        private String code;
        private String queue;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getQueue() {
            return queue;
        }

        public void setQueue(String queue) {
            this.queue = queue;
        }
    }
}
