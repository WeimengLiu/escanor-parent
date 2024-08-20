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

package com.escanor.shiro.token;

import com.escanor.shiro.exception.JwtExpiredAuthenticationException;
import com.escanor.shiro.exception.JwtVerifyFailAuthenticationException;
import io.jsonwebtoken.ExpiredJwtException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.BearerToken;
import org.apache.shiro.authc.credential.CredentialsMatcher;

public class JwtCredentialsMatcher implements CredentialsMatcher {

    private final Log log = LogFactory.getLog(JwtCredentialsMatcher.class);

    private final JwtTokenTemplate jwtTokenTemplate;

    public JwtCredentialsMatcher(JwtTokenTemplate tokenTemplate) {
        this.jwtTokenTemplate = tokenTemplate;
    }

    @Override
    public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
        BearerToken bearerToken = (BearerToken) token;
        String jwtToken = bearerToken.getToken();

        try {
            jwtTokenTemplate.verifyToken(jwtToken);
            return true;
        } catch (ExpiredJwtException e) {
            log.error(e.getMessage(), e);
            throw new JwtExpiredAuthenticationException(e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new JwtVerifyFailAuthenticationException(e);
        }
    }
}
