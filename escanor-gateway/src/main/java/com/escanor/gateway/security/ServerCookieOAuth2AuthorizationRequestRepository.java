/*
 * Copyright 2020-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this artifact or file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.escanor.gateway.security;

import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.oauth2.client.web.server.ServerAuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.*;


public class ServerCookieOAuth2AuthorizationRequestRepository implements ServerAuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    static final Logger log = LoggerFactory.getLogger(ServerCookieOAuth2AuthorizationRequestRepository.class);

    private static final String AUTHORIZATION_REQUEST_COOKIE_NAME = "escanor_oauth2_authorization_request";

    @Override
    public Mono<OAuth2AuthorizationRequest> loadAuthorizationRequest(ServerWebExchange exchange) {
        Assert.notNull(exchange, "exchange cannot be null");

        return Mono.justOrEmpty(exchange.getRequest().getCookies().getFirst(AUTHORIZATION_REQUEST_COOKIE_NAME))
                .filter(Objects::nonNull).map(this::deserialize);
    }

    @Override
    public Mono<Void> saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, ServerWebExchange exchange) {
        Assert.notNull(exchange, "exchange cannot be null");
        return Mono.justOrEmpty(authorizationRequest)
                .switchIfEmpty(Mono.fromRunnable(() -> deleteCookies(exchange)))
                .doOnNext(auth2AuthorizationRequest -> {
                    ResponseCookie cookie = ResponseCookie.from(AUTHORIZATION_REQUEST_COOKIE_NAME, serialize(auth2AuthorizationRequest))
                            .path("/").httpOnly(true).build();
                    exchange.getResponse().addCookie(cookie);
                }).then();
    }

    public static void deleteCookies(ServerWebExchange exchange) {
        MultiValueMap<String, HttpCookie> cookies = exchange.getRequest().getCookies();
        ServerHttpResponse response = exchange.getResponse();
        for (Map.Entry<String, List<HttpCookie>> cookie : cookies.entrySet()) {
            for (HttpCookie cookieToBeDeleted : cookie.getValue()) {
                if (AUTHORIZATION_REQUEST_COOKIE_NAME.equals(cookieToBeDeleted.getName())) {
                    log.debug("Deleting cookie: {} having value: {}", cookieToBeDeleted.getName(), cookieToBeDeleted
                            .getValue());
                    response.addCookie(ResponseCookie.from(cookieToBeDeleted.getName(), cookieToBeDeleted.getValue())
                            .maxAge(0).build());
                }
            }
        }
        log.debug("Response cookies: {}" , response.getCookies());
    }

    @Override
    public Mono<OAuth2AuthorizationRequest> removeAuthorizationRequest(ServerWebExchange exchange) {
        Assert.notNull(exchange, "exchange cannot be null");

        return loadAuthorizationRequest(exchange).doOnNext(auth2AuthorizationRequest -> deleteCookies(exchange));
    }


    private String serialize(OAuth2AuthorizationRequest authorizationRequest) {
        return Base64.getUrlEncoder().encodeToString(
                SerializationUtils.serialize(authorizationRequest));
    }

    private OAuth2AuthorizationRequest deserialize(HttpCookie cookie) {
        return SerializationUtils.deserialize(
                Base64.getUrlDecoder().decode(cookie.getValue()));
    }
}
