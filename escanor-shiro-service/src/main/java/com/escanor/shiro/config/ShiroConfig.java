/*
 * Copyright (c) 2024 Weimeng Liu
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

package com.escanor.shiro.config;

import com.escanor.shiro.client.UserServiceClient;
import com.escanor.shiro.filters.ConsulBasicHttpAuthenticationFilter;
import com.escanor.shiro.filters.JwtFormAuthenticationFilter;
import com.escanor.shiro.filters.JwtHttpAuthenticationFilter;
import com.escanor.shiro.realm.ConsulUsernamePasswordAuthRealm;
import com.escanor.shiro.realm.FirstFailReturnAuthenticationStrategy;
import com.escanor.shiro.realm.UsernamePasswordAuthRealm;
import com.escanor.shiro.token.JwtTokenTemplate;
import org.apache.shiro.authc.pam.AllSuccessfulStrategy;
import org.apache.shiro.authc.pam.AuthenticationStrategy;
import org.apache.shiro.authc.pam.FirstSuccessfulStrategy;
import org.apache.shiro.authz.Authorizer;
import org.apache.shiro.authz.ModularRealmAuthorizer;
import org.apache.shiro.mgt.SessionsSecurityManager;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

@Configuration
@EnableConfigurationProperties(ShiroProperties.class)
public class ShiroConfig {
    @Bean
    public UsernamePasswordAuthRealm authRealm(UserServiceClient userServiceClient) {
        return new UsernamePasswordAuthRealm(userServiceClient);
    }

    @Bean
    ConsulUsernamePasswordAuthRealm usernamePasswordAuthRealm(ShiroProperties shiroProperties) {
        ShiroProperties.User user = shiroProperties.getConsulUser();
        return new ConsulUsernamePasswordAuthRealm(user.getUsername(), user.getPassword());
    }

    @Bean
    Jackson2ObjectMapperBuilderCustomizer customizer() {
        return objectMapperBuilder -> objectMapperBuilder.simpleDateFormat("yyyy-MM-dd hh:mm:ss").timeZone(TimeZone.getTimeZone("Asia/Shanghai"));
    }

    @Bean
    JwtTokenTemplate jwtTokenTemplate(ShiroProperties shiroProperties) {
        return new JwtTokenTemplate(shiroProperties.getSecretKey(), shiroProperties.getTokenTimeOut());
    }

    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(SessionsSecurityManager securityManager, JwtTokenTemplate tokenTemplate) {
        ShiroFilterFactoryBean factoryBean = new ShiroFilterFactoryBean();
        factoryBean.setSecurityManager(securityManager);
        //配置自定义filter
        Map<String, Filter> filterMap = new HashMap<>();
        filterMap.put("jwtAuth", new JwtHttpAuthenticationFilter());//定义filter
        filterMap.put("healthCheckAuth", new ConsulBasicHttpAuthenticationFilter());//定义filter
        filterMap.put("jwtLogin", new JwtFormAuthenticationFilter(tokenTemplate));//定义filter


        factoryBean.setFilters(filterMap);

        Map<String, String> map = new HashMap<>();
        map.put("/actuator/**", "healthCheckAuth");
        map.put("/login", "jwtLogin");
        map.put("/**", "jwtAuth");
        factoryBean.setFilterChainDefinitionMap(map);
        //设置登录页面
        //factoryBean.setLoginUrl("/auth/login");

        //设置无状态，不创建session
        factoryBean.setGlobalFilters(Collections.singletonList("noSessionCreation"));

        return factoryBean;
    }

    @Bean
    public Authorizer authorizer() {
        return new ModularRealmAuthorizer();
    }

    @Bean
    protected AuthenticationStrategy authenticationStrategy() {

        return new FirstFailReturnAuthenticationStrategy();
    }

}
