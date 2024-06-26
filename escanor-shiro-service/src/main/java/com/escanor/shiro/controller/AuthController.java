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

package com.escanor.shiro.controller;

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
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
public class AuthController {

    final AuthService authService;
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/{url}")
    public String redirect(@PathVariable("url") String url) {
        return url;
    }

    @PostMapping("/login")
    public Response<?> login(String username, String password) {
        return authService.login(username, password);
    }

    @PostMapping("/loginFromBrowser")
    public Response<?> loginFromBrowser(String username, String password) {
        Response<?> response = authService.login(username, password);
        return response;
    }


    @RequestMapping("/unauth")
    @ResponseBody
    public String unauth() {
        return "未授权没有访问权限";
    }
}
