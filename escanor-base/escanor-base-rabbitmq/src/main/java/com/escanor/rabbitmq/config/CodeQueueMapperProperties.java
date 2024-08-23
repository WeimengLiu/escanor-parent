package com.escanor.rabbitmq.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Setter
@Getter
@ConfigurationProperties(prefix = "spring.rabbitmq")
public class CodeQueueMapperProperties {

    private Map<String, CodeQueueMappers> bindings = new HashMap<>();

    @Setter
    @Getter
    public static class CodeQueueMappers {

        private String defaultKey;
        private String defaultQueue;

        private List<CodeQueueMapper> mappers = new ArrayList<>();

    }

    @Setter
    @Getter
    public static class CodeQueueMapper {
        private String code;
        private String queue;

    }
}
