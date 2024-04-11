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

import com.escanor.core.common.ErrorResponse;
import com.escanor.core.common.SuccessResponse;
import com.escanor.core.exception.ResponseException;
import com.escanor.shiro.dto.UserInfoDto;
import com.escanor.shiro.exception.AuthenticationFailCode;
import com.escanor.shiro.exception.WrapAuthenticationException;
import com.escanor.shiro.token.JwtTokenTemplate;
import com.escanor.shiro.util.HttpResponseHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtFormAuthenticationFilter extends FormAuthenticationFilter {

    private static final Log log = LogFactory.getLog(JwtFormAuthenticationFilter.class);

    private static final String LOGIN_URL = "/login";

    private final JwtTokenTemplate tokenTemplate;

    public JwtFormAuthenticationFilter(JwtTokenTemplate tokenTemplate) {
        this.tokenTemplate = tokenTemplate;
        setLoginUrl(LOGIN_URL);
    }

    @Override
    protected void saveRequestAndRedirectToLogin(ServletRequest request, ServletResponse response) throws IOException {
        //jwt登录方式，无需保存请求，以及跳转到登录页面，重写该方法
        //nothing to do
    }

    @Override
    protected boolean onLoginSuccess(AuthenticationToken token, Subject subject, ServletRequest request, ServletResponse response) throws Exception {
        UserInfoDto userInfoDto = (UserInfoDto) subject.getPrincipal();
        String jwtToken = tokenTemplate.createJsonWebToken(userInfoDto.toJwtClaims(), String.valueOf(userInfoDto.getId()));
        //页面登录，Token只放cookie中
        setCookie(jwtToken, request, response);
        HttpResponseHelper.writeScOkResponse(response, SuccessResponse.from(userInfoDto.toJwtClaims(), "login success"));
        //we handled the success response directly, prevent the chain from continuing:
        return false;
    }

    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
        try {
            log.error("授权异常信息", e);
            Throwable toUse = e;
            if (e instanceof WrapAuthenticationException) {
                toUse = e.getCause();
            }
            AuthenticationFailCode authenticationFailCode = AuthenticationFailCode.from(toUse.getClass());
            HttpResponseHelper.writeScOkResponse(response, ErrorResponse.fromCodeAndErrorMessage(authenticationFailCode.getCode(), authenticationFailCode.getMessage()));
        } catch (IOException ex) {
            throw new ResponseException("write response fail", ex);
        }
        return false;
    }


    protected void setCookie(String jwtToken, ServletRequest request, ServletResponse response) {
        if (WebUtils.isHttp(new SimpleRequestPairSource(request, response))) {
            HttpServletResponse servletResponse = WebUtils.toHttp(response);
            Cookie cookie = new Cookie("Authorization", jwtToken);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            servletResponse.addCookie(cookie);
        }
    }
}
