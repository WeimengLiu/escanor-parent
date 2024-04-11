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

package com.escanor.shiro.dto;

import com.escanor.core.dto.BaseDto;
import com.escanor.core.util.CollectionUtils;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoDto extends BaseDto {

    static final String KEY_USERNAME = "username";
    //static final String KEY_PASSWORD = "password";
    static final String KEY_PERMS = "perms";
    static final String KEY_ROLE = "role";


    private String username;
    private String password;
    private String perms;
    private String role;

    public static UserInfoDto fromJwtClaims(Map<String, Object> claims) {
        if (CollectionUtils.isNotEmpty(claims)) {
            return UserInfoDto.builder().username((String) claims.get(KEY_USERNAME)).perms((String) claims.get(KEY_PERMS)).role((String) claims.get(KEY_ROLE)).build();
        }
        return null;
    }

    public Map<String, Object> toJwtClaims() {
        Map<String, Object> claims = new HashMap<>();
        claims.put(KEY_USERNAME, username);
        //claims.put(KEY_PASSWORD, password);
        claims.put(KEY_PERMS, perms);
        claims.put(KEY_ROLE, role);
        return claims;
    }
}
