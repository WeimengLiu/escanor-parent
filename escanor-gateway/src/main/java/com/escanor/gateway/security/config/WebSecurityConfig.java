package com.escanor.gateway.security.config;

import com.escanor.gateway.security.matcher.PermitUrlMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.util.StringUtils;

import java.util.List;

@Configuration
public class WebSecurityConfig {

    private PermitUrlMatcher permitUrlMatcher;

    @Autowired
    public void setPermitUrlMatcher(PermitUrlMatcher permitUrlMatcher) {
        this.permitUrlMatcher = permitUrlMatcher;
    }


    public ReactiveAuthenticationManager createDefaultReactiveAuthenticationManager(SecurityProperties securityProperties) {
        SecurityProperties.User user = securityProperties.getUser();
        List<String> roles = user.getRoles();
        UserDetails userDetails = User.withUsername(user.getName()).password("{noop}" + user.getPassword()).roles(StringUtils.toStringArray(roles)).build();
        MapReactiveUserDetailsService mapReactiveUserDetailsService = new MapReactiveUserDetailsService(userDetails);
        return new UserDetailsRepositoryReactiveAuthenticationManager(mapReactiveUserDetailsService);
    }


    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http, SecurityProperties securityProperties) {
        http.authenticationManager(createDefaultReactiveAuthenticationManager(securityProperties)).authorizeExchange()
                //.pathMatchers("/actuator/health").hasRole("USER")
                .pathMatchers("/actuator/**").hasRole("CONSUL").matchers(permitUrlMatcher).permitAll().anyExchange().authenticated().and().httpBasic().and().csrf().disable().oauth2Login().and().exceptionHandling();
        return http.build();
    }
}
