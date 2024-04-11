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

package com.escanor.shiro.filters;

import com.escanor.shiro.token.ConsulUsernamePasswordToken;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class ConsulBasicHttpAuthenticationFilter extends BasicHttpAuthenticationFilter {

    private static final Logger log = LoggerFactory.getLogger(ConsulBasicHttpAuthenticationFilter.class);


    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) {
        String authorizationHeader = getAuthzHeader(request);
        if (authorizationHeader == null || authorizationHeader.isEmpty()) {
            // Create an empty authentication token since there is no
            // Authorization header.
            return createConsulToken("", "", request, response);
        }

        log.debug("Attempting to execute login with auth header");

        String[] prinCred = getPrincipalsAndCredentials(authorizationHeader, request);
        if (prinCred == null || prinCred.length < 2) {
            // Create an authentication token with an empty password,
            // since one hasn't been provided in the request.
            String username = prinCred == null || prinCred.length == 0 ? "" : prinCred[0];
            return createConsulToken(username, "", request, response);
        }

        String username = prinCred[0];
        String password = prinCred[1];

        return createConsulToken(username, password, request, response);
    }

    protected AuthenticationToken createConsulToken(String username, String password, ServletRequest request, ServletResponse response) {
        boolean rememberMe = isRememberMe(request);
        String host = getHost(request);
        return new ConsulUsernamePasswordToken(username, password, rememberMe, host);
    }

}
