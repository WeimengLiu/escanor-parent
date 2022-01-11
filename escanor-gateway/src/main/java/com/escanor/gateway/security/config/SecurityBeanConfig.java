package com.escanor.gateway.security.config;

import com.escanor.gateway.security.matcher.PermitUrlMatcher;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(GatewaySecurityProperties.class)
public class SecurityBeanConfig {

    @Bean
    @RefreshScope
    PermitUrlMatcher permitUrlMatcher(GatewaySecurityProperties securityProperties) {
        return new PermitUrlMatcher(securityProperties.getPermitUrls());
    }
}
