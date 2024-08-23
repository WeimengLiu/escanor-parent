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

import com.escanor.shiro.dto.UserInfoDto;
import com.escanor.shiro.token.JwtCredentialsMatcher;
import com.escanor.shiro.token.JwtTokenTemplate;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenAuthRealm extends AuthorizingRealm {

    final JwtTokenTemplate jwtTokenTemplate;

    public JwtTokenAuthRealm(JwtTokenTemplate jwtTokenTemplate) {
        this.jwtTokenTemplate = jwtTokenTemplate;
        setCredentialsMatcher(new JwtCredentialsMatcher(jwtTokenTemplate));
        setAuthenticationTokenClass(BearerToken.class);
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        BearerToken bearerToken = (BearerToken) token;
        String jwtToken = bearerToken.getToken();
        UserInfoDto userInfoDto = UserInfoDto.fromJwtClaims(jwtTokenTemplate.parseToken(jwtToken));

        return new SimpleAuthenticationInfo(userInfoDto, bearerToken, getName());
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {

        UserInfoDto user = (UserInfoDto) SecurityUtils.getSubject().getPrincipal();

        SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
        simpleAuthorizationInfo.addRole(user.getRole()); //roles跟着user走，放到token里。普通功能直接用token中的。只有在重要操作时才需去数据库验一遍，减轻压力
        simpleAuthorizationInfo.addStringPermission(user.getPerms());
        return simpleAuthorizationInfo;
    }
}
