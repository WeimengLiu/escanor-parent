package com.escanor.gateway.config;

import com.escanor.gateway.util.IPUtils;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class GatewayConfig {

    @Bean
    KeyResolver userKeyResolver() {
        return exchange -> {
            if (null != exchange.getRequest().getRemoteAddress()) {
                return Mono.just(IPUtils.getIp(exchange));
            } else {
                return Mono.just("localhost");
            }
        };
    }
}
