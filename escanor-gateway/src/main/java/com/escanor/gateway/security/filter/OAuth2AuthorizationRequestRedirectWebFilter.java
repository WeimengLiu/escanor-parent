/*
 * Copyright (c) 2022 Weimeng Liu
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.escanor.gateway.security.filter;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.client.ClientAuthorizationRequiredException;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.server.DefaultServerOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.server.ServerAuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.server.WebSessionOAuth2ServerAuthorizationRequestRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.server.DefaultServerRedirectStrategy;
import org.springframework.security.web.server.ServerRedirectStrategy;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import org.springframework.security.web.server.savedrequest.WebSessionServerRequestCache;
import org.springframework.util.Assert;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * This {@code WebFilter} initiates the authorization code grant or implicit grant flow
 * by redirecting the End-User's user-agent to the Authorization Server's Authorization Endpoint.
 *
 * <p>
 * It builds the OAuth 2.0 Authorization Request,
 * which is used as the redirect {@code URI} to the Authorization Endpoint.
 * The redirect {@code URI} will include the client identifier, requested scope(s), state,
 * response type, and a redirection URI which the authorization server will send the user-agent back to
 * once access is granted (or denied) by the End-User (Resource Owner).
 *
 * <p>
 * By default, this {@code Filter} responds to authorization requests
 * at the {@code URI} {@code /oauth2/authorization/{registrationId}}.
 * The {@code URI} template variable {@code {registrationId}} represents the
 * {@link ClientRegistration#getRegistrationId() registration identifier} of the client
 * that is used for initiating the OAuth 2.0 Authorization Request.
 *
 * @author Rob Winch
 * @see OAuth2AuthorizationRequest
 * @see AuthorizationRequestRepository
 * @see ClientRegistration
 * @see ClientRegistrationRepository
 * @see <a target="_blank" href="https://tools.ietf.org/html/rfc6749#section-4.1">Section 4.1 Authorization Code Grant</a>
 * @see <a target="_blank" href="https://tools.ietf.org/html/rfc6749#section-4.1.1">Section 4.1.1 Authorization Request (Authorization Code)</a>
 * @see <a target="_blank" href="https://tools.ietf.org/html/rfc6749#section-4.2">Section 4.2 Implicit Grant</a>
 * @see <a target="_blank" href="https://tools.ietf.org/html/rfc6749#section-4.2.1">Section 4.2.1 Authorization Request (Implicit)</a>
 * @since 5.1
 */
public class OAuth2AuthorizationRequestRedirectWebFilter implements WebFilter {

    public static final String DEFAULT_FILTER_PROCESSES_URI = "/login/oauth2/code/{registrationId}";


    private final ServerRedirectStrategy authorizationRedirectStrategy = new DefaultServerRedirectStrategy();
    private final ServerOAuth2AuthorizationRequestResolver authorizationRequestResolver;
    private ServerAuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository =
            new WebSessionOAuth2ServerAuthorizationRequestRepository();
    private ServerRequestCache requestCache = new WebSessionServerRequestCache();

    /**
     * Constructs an {@code OAuth2AuthorizationRequestRedirectFilter} using the provided parameters.
     *
     * @param clientRegistrationRepository the repository of client registrations
     */
    public OAuth2AuthorizationRequestRedirectWebFilter(ReactiveClientRegistrationRepository clientRegistrationRepository) {
        this.authorizationRequestResolver = new DefaultServerOAuth2AuthorizationRequestResolver(clientRegistrationRepository);
    }

    /**
     * Constructs an {@code OAuth2AuthorizationRequestRedirectFilter} using the provided parameters.
     *
     * @param authorizationRequestResolver the resolver to use
     */
    public OAuth2AuthorizationRequestRedirectWebFilter(ServerOAuth2AuthorizationRequestResolver authorizationRequestResolver) {
        Assert.notNull(authorizationRequestResolver, "authorizationRequestResolver cannot be null");
        this.authorizationRequestResolver = authorizationRequestResolver;
    }

    /**
     * Sets the repository used for storing {@link OAuth2AuthorizationRequest}'s.
     *
     * @param authorizationRequestRepository the repository used for storing {@link OAuth2AuthorizationRequest}'s
     */
    public final void setAuthorizationRequestRepository(
            ServerAuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository) {
        Assert.notNull(authorizationRequestRepository, "authorizationRequestRepository cannot be null");
        this.authorizationRequestRepository = authorizationRequestRepository;
    }

    /**
     * The request cache to use to save the request before sending a redirect.
     *
     * @param requestCache the cache to redirect to.
     */
    public void setRequestCache(ServerRequestCache requestCache) {
        Assert.notNull(requestCache, "requestCache cannot be null");
        this.requestCache = requestCache;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return this.authorizationRequestResolver.resolve(exchange)
                .switchIfEmpty(chain.filter(exchange).then(Mono.empty()))
                .onErrorResume(ClientAuthorizationRequiredException.class, e -> this.requestCache.saveRequest(exchange)
                        .then(this.authorizationRequestResolver.resolve(exchange, e.getClientRegistrationId())))
                .flatMap(clientRegistration -> sendForwardAuthorization(exchange, clientRegistration, chain));
    }

    private Mono<Void> sendForwardAuthorization(ServerWebExchange exchange,
                                                OAuth2AuthorizationRequest authorizationRequest, WebFilterChain chain) {
        return Mono.defer(() -> {
            Mono<Void> saveAuthorizationRequest = Mono.empty();
            if (AuthorizationGrantType.AUTHORIZATION_CODE.equals(authorizationRequest.getGrantType())) {
                saveAuthorizationRequest = this.authorizationRequestRepository
                        .saveAuthorizationRequest(authorizationRequest, exchange);
            }
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpRequest.Builder builder = request.mutate();
            URI uri = UriComponentsBuilder.fromHttpRequest(request).build(true).toUri();
            builder.uri(uri);
            builder.path(DEFAULT_FILTER_PROCESSES_URI);

            return saveAuthorizationRequest.then(chain.filter(exchange.mutate().request(builder.build()).build()));
			/*URI redirectUri = UriComponentsBuilder
					.f romUriString(authorizationRequest.getAuthorizationRequestUri())
					.build(true).toUri();
			return saveAuthorizationRequest
					.then(this.authorizationRedirectStrategy.sendRedirect(exchange, redirectUri));*/
        });
    }
}
