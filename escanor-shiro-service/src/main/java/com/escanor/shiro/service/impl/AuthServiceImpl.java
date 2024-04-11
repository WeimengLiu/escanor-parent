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

package com.escanor.shiro.service.impl;

import com.escanor.core.common.ErrorResponse;
import com.escanor.core.common.Response;
import com.escanor.core.common.SuccessResponse;
import com.escanor.shiro.dto.UserInfoDto;
import com.escanor.shiro.service.AuthService;
import com.escanor.shiro.token.JwtTokenTemplate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Log log = LogFactory.getLog(AuthServiceImpl.class);
    final JwtTokenTemplate tokenTemplate;

    public AuthServiceImpl(JwtTokenTemplate tokenTemplate) {
        this.tokenTemplate = tokenTemplate;
    }

    @Override
    public Response<?> login(String username, String password) {
        Assert.hasLength(username, "username not allow null");
        Assert.hasLength(password, "password not allow null");

        Subject subject = SecurityUtils.getSubject();
        UsernamePasswordToken token = new UsernamePasswordToken(username, password);
        try {
            subject.login(token);
            UserInfoDto userInfoDto = (UserInfoDto) subject.getPrincipal();

            String jwtToken = tokenTemplate.createJsonWebToken(userInfoDto.toJwtClaims(), String.valueOf(userInfoDto.getId()));

            return SuccessResponse.from(jwtToken, "login success");
        } catch (Exception e) {
            log.error("[" + username + "]" + " login fail", e);
            return ErrorResponse.fromErrorMessage("login fail! please check username and password");
        }
    }
}
