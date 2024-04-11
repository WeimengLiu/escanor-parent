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

package com.escanor.shiro.realm;

import com.escanor.shiro.token.ConsulUsernamePasswordToken;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.realm.AuthenticatingRealm;

public class ConsulUsernamePasswordAuthRealm extends AuthenticatingRealm {

    private final String username;
    private final String password;

    public ConsulUsernamePasswordAuthRealm(String username, String password) {
        this.password = password;
        this.username = username;
        setAuthenticationTokenClass(ConsulUsernamePasswordToken.class);
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) {
        String inputUsername = (String) token.getPrincipal();
        if (StringUtils.equals(username, inputUsername)) {
            return new SimpleAuthenticationInfo(username, password, getName());
        } else {
            throw new UnknownAccountException("账户不存在!");
        }
    }

}
