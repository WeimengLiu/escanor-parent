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

import com.escanor.core.common.Response;
import com.escanor.core.exception.ResponseException;
import com.escanor.shiro.exception.AuthenticationFailCode;
import com.escanor.shiro.util.HttpResponseHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.BearerHttpAuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.stream.Stream;


public class JwtHttpAuthenticationFilter extends BearerHttpAuthenticationFilter {

    private static final Log log = LogFactory.getLog(JwtHttpAuthenticationFilter.class);


    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) {
        SimpleRequestPairSource simpleRequestPairSource = new SimpleRequestPairSource(request, response);
        if (WebUtils.isHttp(simpleRequestPairSource)) {
            HttpServletRequest httpServletRequest = WebUtils.toHttp(request);
            String token = Stream.of(httpServletRequest.getCookies()).filter(cookie -> StringUtils.equals(cookie.getName(), AUTHORIZATION_HEADER)).map(Cookie::getValue).findFirst().orElse(null);
            if (StringUtils.isNotBlank(token)) {
                return createBearerToken(token, request);
            }
        }
        return super.createToken(request, response);
    }

    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
        try {
            log.error("授权异常信息", e);
            AuthenticationFailCode authenticationFailCode = AuthenticationFailCode.from(e.getClass());
            HttpResponseHelper.writeScOkResponse(response, Response.fail(authenticationFailCode.getCode(), authenticationFailCode.getMessage()));
        } catch (IOException ex) {
            throw new ResponseException("write response fail", ex);
        }
        return false;
    }
}
