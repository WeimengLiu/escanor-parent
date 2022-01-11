package com.escanor.gateway.security.matcher;

import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class PermitUrlMatcher implements ServerWebExchangeMatcher {

    private final ServerWebExchangeMatcher permitAllUrlMatchers;

    public PermitUrlMatcher(String[] permitAllUrls) {
        this.permitAllUrlMatchers = ServerWebExchangeMatchers.pathMatchers(permitAllUrls);
    }

    @Override
    public Mono<MatchResult> matches(ServerWebExchange exchange) {
        return permitAllUrlMatchers.matches(exchange);
    }
}
