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

package com.escanor.shiro.exception;

import lombok.Getter;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;

import java.util.stream.Stream;

@Getter
public enum AuthenticationFailCode {

    AuthenticationFail(1000, "授权失败", AuthenticationException.class),
    UnknownAccount(1001, "用户名或者密码错误", UnknownAccountException.class),
    IncorrectCredentials(1002, "用户名或者密码错误", IncorrectCredentialsException.class),
    JwtVerifyFail(1003, "Token校验失败", JwtVerifyFailAuthenticationException.class),
    JwtExpired(1004, "Token过期", JwtExpiredAuthenticationException.class);


    private final int code;
    private final String message;
    private final Class<? extends AuthenticationException> exceptionClass;

    AuthenticationFailCode(int code, String message, Class<? extends AuthenticationException> exceptionClass) {
        this.code = code;
        this.message = message;
        this.exceptionClass = exceptionClass;
    }

    public static AuthenticationFailCode from(Class<? extends Throwable> exceptionClass) {
        return Stream.of(values()).filter(authenticationFailCode -> authenticationFailCode.getExceptionClass().equals(exceptionClass)).findFirst().orElse(AuthenticationFail);
    }
}
