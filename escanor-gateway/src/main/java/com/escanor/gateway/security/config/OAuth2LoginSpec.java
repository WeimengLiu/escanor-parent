package com.escanor.gateway.security.config;

import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.security.authentication.DelegatingReactiveAuthenticationManager;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.InMemoryReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthorizationCodeReactiveAuthenticationManager;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginReactiveAuthenticationManager;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.ReactiveOAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.WebClientReactiveAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.oidc.authentication.OidcAuthorizationCodeReactiveAuthenticationManager;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcReactiveOAuth2UserService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultReactiveOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.ReactiveOAuth2UserService;
import org.springframework.security.oauth2.client.web.server.*;
import org.springframework.security.oauth2.client.web.server.authentication.OAuth2LoginAuthenticationWebFilter;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoderFactory;
import org.springframework.security.web.server.DelegatingServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.*;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.security.web.server.savedrequest.NoOpServerRequestCache;
import org.springframework.security.web.server.util.matcher.*;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class OAuth2LoginSpec {
    private ReactiveClientRegistrationRepository clientRegistrationRepository;

    private ServerOAuth2AuthorizedClientRepository authorizedClientRepository;

    private ServerAuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository;

    private ReactiveAuthenticationManager authenticationManager;

    private ServerSecurityContextRepository securityContextRepository;

    private ServerAuthenticationConverter authenticationConverter;

    private ServerOAuth2AuthorizationRequestResolver authorizationRequestResolver;

    private ServerWebExchangeMatcher authenticationMatcher;

    private ServerAuthenticationSuccessHandler authenticationSuccessHandler;

    private ServerAuthenticationFailureHandler authenticationFailureHandler;

    private ApplicationContext context;

    /**
     * Configures the {@link ReactiveAuthenticationManager} to use. The default is
     * {@link OAuth2AuthorizationCodeReactiveAuthenticationManager}
     * @param authenticationManager the manager to use
     * @return the {@link OAuth2LoginSpec} to customize
     */
    public OAuth2LoginSpec authenticationManager(ReactiveAuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
        return this;
    }

    /**
     * The {@link ServerSecurityContextRepository} used to save the {@code Authentication}. Defaults to
     * {@link WebSessionServerSecurityContextRepository}.
     *
     * @since 5.2
     * @param securityContextRepository the repository to use
     * @return the {@link OAuth2LoginSpec} to continue configuring
     */
    public OAuth2LoginSpec securityContextRepository(ServerSecurityContextRepository securityContextRepository) {
        this.securityContextRepository = securityContextRepository;
        return this;
    }

    /**
     * The {@link ServerAuthenticationSuccessHandler} used after authentication success. Defaults to
     * {@link RedirectServerAuthenticationSuccessHandler} redirecting to "/".
     *
     * @since 5.2
     * @param authenticationSuccessHandler the success handler to use
     * @return the {@link OAuth2LoginSpec} to customize
     */
    public OAuth2LoginSpec authenticationSuccessHandler(ServerAuthenticationSuccessHandler authenticationSuccessHandler) {
        Assert.notNull(authenticationSuccessHandler, "authenticationSuccessHandler cannot be null");
        this.authenticationSuccessHandler = authenticationSuccessHandler;
        return this;
    }

    /**
     * The {@link ServerAuthenticationFailureHandler} used after authentication failure.
     * Defaults to {@link RedirectServerAuthenticationFailureHandler} redirecting to "/login?error".
     *
     * @since 5.2
     * @param authenticationFailureHandler the failure handler to use
     * @return the {@link OAuth2LoginSpec} to customize
     */
    public OAuth2LoginSpec authenticationFailureHandler(ServerAuthenticationFailureHandler authenticationFailureHandler) {
        Assert.notNull(authenticationFailureHandler, "authenticationFailureHandler cannot be null");
        this.authenticationFailureHandler = authenticationFailureHandler;
        return this;
    }

    /**
     * Gets the {@link ReactiveAuthenticationManager} to use. First tries an explicitly configured manager, and
     * defaults to {@link OAuth2AuthorizationCodeReactiveAuthenticationManager}
     *
     * @return the {@link ReactiveAuthenticationManager} to use
     */
    private ReactiveAuthenticationManager getAuthenticationManager() {
        if (this.authenticationManager == null) {
            this.authenticationManager = createDefault();
        }
        return this.authenticationManager;
    }

    private ReactiveAuthenticationManager createDefault() {
        ReactiveOAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> client = getAccessTokenResponseClient();
        ReactiveAuthenticationManager result = new OAuth2LoginReactiveAuthenticationManager(client, getOauth2UserService());

        boolean oidcAuthenticationProviderEnabled = ClassUtils.isPresent(
                "org.springframework.security.oauth2.jwt.JwtDecoder", this.getClass().getClassLoader());
        if (oidcAuthenticationProviderEnabled) {
            OidcAuthorizationCodeReactiveAuthenticationManager oidc =
                    new OidcAuthorizationCodeReactiveAuthenticationManager(client, getOidcUserService());
            ResolvableType type = ResolvableType.forClassWithGenerics(
                    ReactiveJwtDecoderFactory.class, ClientRegistration.class);
            ReactiveJwtDecoderFactory<ClientRegistration> jwtDecoderFactory = getBeanOrNull(type);
            if (jwtDecoderFactory != null) {
                oidc.setJwtDecoderFactory(jwtDecoderFactory);
            }
            result = new DelegatingReactiveAuthenticationManager(oidc, result);
        }
        return result;
    }

    /**
     * Sets the converter to use
     * @param authenticationConverter the converter to use
     * @return the {@link OAuth2LoginSpec} to customize
     */
    public OAuth2LoginSpec authenticationConverter(ServerAuthenticationConverter authenticationConverter) {
        this.authenticationConverter = authenticationConverter;
        return this;
    }

    private ServerAuthenticationConverter getAuthenticationConverter(ReactiveClientRegistrationRepository clientRegistrationRepository) {
        if (this.authenticationConverter == null) {
            ServerOAuth2AuthorizationCodeAuthenticationTokenConverter delegate =
                    new ServerOAuth2AuthorizationCodeAuthenticationTokenConverter(clientRegistrationRepository);
            delegate.setAuthorizationRequestRepository(getAuthorizationRequestRepository());
            ServerAuthenticationConverter authenticationConverter = exchange ->
                    delegate.convert(exchange).onErrorMap(OAuth2AuthorizationException.class,
                            e -> new OAuth2AuthenticationException(e.getError(), e.getError().toString()));
            this.authenticationConverter = authenticationConverter;
            return authenticationConverter;
        }
        return this.authenticationConverter;
    }

    public OAuth2LoginSpec clientRegistrationRepository(ReactiveClientRegistrationRepository clientRegistrationRepository) {
        this.clientRegistrationRepository = clientRegistrationRepository;
        return this;
    }

    public OAuth2LoginSpec authorizedClientService(ReactiveOAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientRepository = new AuthenticatedPrincipalServerOAuth2AuthorizedClientRepository(authorizedClientService);
        return this;
    }

    public OAuth2LoginSpec authorizedClientRepository(ServerOAuth2AuthorizedClientRepository authorizedClientRepository) {
        this.authorizedClientRepository = authorizedClientRepository;
        return this;
    }

    /**
     * Sets the repository to use for storing {@link OAuth2AuthorizationRequest}'s.
     *
     * @since 5.2
     * @param authorizationRequestRepository the repository to use for storing {@link OAuth2AuthorizationRequest}'s
     * @return the {@link OAuth2LoginSpec} for further configuration
     */
    public OAuth2LoginSpec authorizationRequestRepository(
            ServerAuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository) {
        this.authorizationRequestRepository = authorizationRequestRepository;
        return this;
    }

    /**
     * Sets the resolver used for resolving {@link OAuth2AuthorizationRequest}'s.
     *
     * @since 5.2
     * @param authorizationRequestResolver the resolver used for resolving {@link OAuth2AuthorizationRequest}'s
     * @return the {@link OAuth2LoginSpec} for further configuration
     */
    public OAuth2LoginSpec authorizationRequestResolver(ServerOAuth2AuthorizationRequestResolver authorizationRequestResolver) {
        this.authorizationRequestResolver = authorizationRequestResolver;
        return this;
    }

    /**
     * Sets the {@link ServerWebExchangeMatcher matcher} used for determining if the request is an authentication request.
     *
     * @since 5.2
     * @param authenticationMatcher the {@link ServerWebExchangeMatcher matcher} used for determining if the request is an authentication request
     * @return the {@link OAuth2LoginSpec} for further configuration
     */
    public OAuth2LoginSpec authenticationMatcher(ServerWebExchangeMatcher authenticationMatcher) {
        this.authenticationMatcher = authenticationMatcher;
        return this;
    }

    private ServerWebExchangeMatcher getAuthenticationMatcher() {
        if (this.authenticationMatcher == null) {
            this.authenticationMatcher = createAttemptAuthenticationRequestMatcher();
        }
        return this.authenticationMatcher;
    }

    /**
     * Allows method chaining to continue configuring the {@link ServerHttpSecurity}
     * @return the {@link ServerHttpSecurity} to continue configuring
     */
    /*public ServerHttpSecurity and() {
        return ServerHttpSecurity.this;
    }*/


    protected void configure(ServerHttpSecurity http) {
        ReactiveClientRegistrationRepository clientRegistrationRepository = getClientRegistrationRepository();
        ServerOAuth2AuthorizedClientRepository authorizedClientRepository = getAuthorizedClientRepository();
        OAuth2AuthorizationRequestRedirectWebFilter oauthRedirectFilter = getRedirectWebFilter();
        ServerAuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository =
                getAuthorizationRequestRepository();
        oauthRedirectFilter.setAuthorizationRequestRepository(authorizationRequestRepository);
        oauthRedirectFilter.setRequestCache(NoOpServerRequestCache.getInstance());

        ReactiveAuthenticationManager manager = getAuthenticationManager();

        AuthenticationWebFilter authenticationFilter = new OAuth2LoginAuthenticationWebFilter(manager, authorizedClientRepository);
        authenticationFilter.setRequiresAuthenticationMatcher(getAuthenticationMatcher());
        authenticationFilter.setServerAuthenticationConverter(getAuthenticationConverter(clientRegistrationRepository));

        authenticationFilter.setAuthenticationSuccessHandler(getAuthenticationSuccessHandler(http));
        authenticationFilter.setAuthenticationFailureHandler(getAuthenticationFailureHandler());
        authenticationFilter.setSecurityContextRepository(this.securityContextRepository);

        //setDefaultEntryPoints(http);

        http.addFilterAt(oauthRedirectFilter, SecurityWebFiltersOrder.HTTP_BASIC);
        http.addFilterAt(authenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION);
    }

    /*private void setDefaultEntryPoints(ServerHttpSecurity http) {
        String defaultLoginPage = "/login";
        Map<String, String> urlToText = http.oauth2Login.getLinks();
        String providerLoginPage = null;
        if (urlToText.size() == 1) {
            providerLoginPage = urlToText.keySet().iterator().next();
        }

        MediaTypeServerWebExchangeMatcher htmlMatcher = new MediaTypeServerWebExchangeMatcher(
                MediaType.APPLICATION_XHTML_XML, new MediaType("image", "*"),
                MediaType.TEXT_HTML, MediaType.TEXT_PLAIN);
        htmlMatcher.setIgnoredMediaTypes(Collections.singleton(MediaType.ALL));

        ServerWebExchangeMatcher xhrMatcher = exchange -> {
            if (exchange.getRequest().getHeaders().getOrEmpty("X-Requested-With").contains("XMLHttpRequest")) {
                return ServerWebExchangeMatcher.MatchResult.match();
            }
            return ServerWebExchangeMatcher.MatchResult.notMatch();
        };
        ServerWebExchangeMatcher notXhrMatcher = new NegatedServerWebExchangeMatcher(xhrMatcher);

        ServerWebExchangeMatcher defaultEntryPointMatcher = new AndServerWebExchangeMatcher(
                notXhrMatcher, htmlMatcher);

        if (providerLoginPage != null) {
            ServerWebExchangeMatcher loginPageMatcher = new PathPatternParserServerWebExchangeMatcher(defaultLoginPage);
            ServerWebExchangeMatcher faviconMatcher = new PathPatternParserServerWebExchangeMatcher("/favicon.ico");
            ServerWebExchangeMatcher defaultLoginPageMatcher = new AndServerWebExchangeMatcher(
                    new OrServerWebExchangeMatcher(loginPageMatcher, faviconMatcher), defaultEntryPointMatcher);

            ServerWebExchangeMatcher matcher = new AndServerWebExchangeMatcher(
                    notXhrMatcher, new NegatedServerWebExchangeMatcher(defaultLoginPageMatcher));
            RedirectServerAuthenticationEntryPoint entryPoint =
                    new RedirectServerAuthenticationEntryPoint(providerLoginPage);
            entryPoint.setRequestCache(http.requestCache.requestCache);
            http.defaultEntryPoints.add(new DelegatingServerAuthenticationEntryPoint.DelegateEntry(matcher, entryPoint));
        }

        RedirectServerAuthenticationEntryPoint defaultEntryPoint =
                new RedirectServerAuthenticationEntryPoint(defaultLoginPage);
        defaultEntryPoint.setRequestCache(http.requestCache.requestCache);
        http.defaultEntryPoints.add(new DelegatingServerAuthenticationEntryPoint.DelegateEntry(defaultEntryPointMatcher, defaultEntryPoint));
    }*/

    private ServerAuthenticationSuccessHandler getAuthenticationSuccessHandler(ServerHttpSecurity http) {
        if (this.authenticationSuccessHandler == null) {
            RedirectServerAuthenticationSuccessHandler handler = new RedirectServerAuthenticationSuccessHandler();
            //handler.setRequestCache(http.requestCache.requestCache);
            this.authenticationSuccessHandler = handler;
        }
        return this.authenticationSuccessHandler;
    }

    private ServerAuthenticationFailureHandler getAuthenticationFailureHandler() {
        if (this.authenticationFailureHandler == null) {
            this.authenticationFailureHandler = new RedirectServerAuthenticationFailureHandler("/login?error");
        }
        return this.authenticationFailureHandler;
    }

    private ServerWebExchangeMatcher createAttemptAuthenticationRequestMatcher() {
        return new PathPatternParserServerWebExchangeMatcher("/login/oauth2/code/{registrationId}");
    }

    private ReactiveOAuth2UserService<OidcUserRequest, OidcUser> getOidcUserService() {
        ResolvableType type = ResolvableType.forClassWithGenerics(ReactiveOAuth2UserService.class, OidcUserRequest.class, OidcUser.class);
        ReactiveOAuth2UserService<OidcUserRequest, OidcUser> bean = getBeanOrNull(type);
        if (bean == null) {
            return new OidcReactiveOAuth2UserService();
        }

        return bean;
    }

    private ReactiveOAuth2UserService<OAuth2UserRequest, OAuth2User> getOauth2UserService() {
        ResolvableType type = ResolvableType.forClassWithGenerics(ReactiveOAuth2UserService.class, OAuth2UserRequest.class, OAuth2User.class);
        ReactiveOAuth2UserService<OAuth2UserRequest, OAuth2User> bean = getBeanOrNull(type);
        if (bean == null) {
            return new DefaultReactiveOAuth2UserService();
        }

        return bean;
    }

    private Map<String, String> getLinks() {
        Iterable<ClientRegistration> registrations = getBeanOrNull(ResolvableType.forClassWithGenerics(Iterable.class, ClientRegistration.class));
        if (registrations == null) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new HashMap<>();
        registrations.iterator().forEachRemaining(r -> result.put("/oauth2/authorization/" + r.getRegistrationId(), r.getClientName()));
        return result;
    }

    private ReactiveOAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> getAccessTokenResponseClient() {
        ResolvableType type = ResolvableType.forClassWithGenerics(ReactiveOAuth2AccessTokenResponseClient.class, OAuth2AuthorizationCodeGrantRequest.class);
        ReactiveOAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> bean = getBeanOrNull(type);
        if (bean == null) {
            return new WebClientReactiveAuthorizationCodeTokenResponseClient();
        }
        return bean;
    }

    private ReactiveClientRegistrationRepository getClientRegistrationRepository() {
        if (this.clientRegistrationRepository == null) {
            this.clientRegistrationRepository = getBeanOrNull(ReactiveClientRegistrationRepository.class);
        }
        return this.clientRegistrationRepository;
    }

    private OAuth2AuthorizationRequestRedirectWebFilter getRedirectWebFilter() {
        OAuth2AuthorizationRequestRedirectWebFilter oauthRedirectFilter;
        if (this.authorizationRequestResolver == null) {
            oauthRedirectFilter = new OAuth2AuthorizationRequestRedirectWebFilter(getClientRegistrationRepository());
        } else {
            oauthRedirectFilter = new OAuth2AuthorizationRequestRedirectWebFilter(this.authorizationRequestResolver);
        }
        return oauthRedirectFilter;
    }

    private ServerOAuth2AuthorizedClientRepository getAuthorizedClientRepository() {
        ServerOAuth2AuthorizedClientRepository result = this.authorizedClientRepository;
        if (result == null) {
            result = getBeanOrNull(ServerOAuth2AuthorizedClientRepository.class);
        }
        if (result == null) {
            ReactiveOAuth2AuthorizedClientService authorizedClientService = getAuthorizedClientService();
            if (authorizedClientService != null) {
                result = new AuthenticatedPrincipalServerOAuth2AuthorizedClientRepository(
                        authorizedClientService);
            }
        }
        return result;
    }

    private ServerAuthorizationRequestRepository<OAuth2AuthorizationRequest> getAuthorizationRequestRepository() {
        if (this.authorizationRequestRepository == null) {
            this.authorizationRequestRepository = new WebSessionOAuth2ServerAuthorizationRequestRepository();
        }
        return this.authorizationRequestRepository;
    }

    private ReactiveOAuth2AuthorizedClientService getAuthorizedClientService() {
        ReactiveOAuth2AuthorizedClientService service = getBeanOrNull(ReactiveOAuth2AuthorizedClientService.class);
        if (service == null) {
            service = new InMemoryReactiveOAuth2AuthorizedClientService(getClientRegistrationRepository());
        }
        return service;
    }

    private <T> T getBeanOrNull(Class<T> beanClass) {
        return getBeanOrNull(ResolvableType.forClass(beanClass));
    }


    private <T> T getBeanOrNull(ResolvableType type) {
        if (this.context == null) {
            return null;
        }
        String[] names =  this.context.getBeanNamesForType(type);
        if (names.length == 1) {
            return (T) this.context.getBean(names[0]);
        }
        return null;
    }

    private OAuth2LoginSpec() {}
}