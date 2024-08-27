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

package com.escanor.user.controller;

import com.escanor.core.common.Request;
import com.escanor.core.common.Response;
import com.escanor.user.dto.UserInfoDto;
import com.escanor.user.dto.mapper.UserInfoMapper;
import com.escanor.user.entity.UserInfoEntity;
import com.escanor.user.service.UserInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/user")
@Api(tags = "用户操作")
public class UserController {

    final UserInfoService userInfoService;

    public UserController(UserInfoService userInfoService) {
        this.userInfoService = userInfoService;
    }

    /**
     * 普通用户查询方法，不返回密码信息
     * @param userName 用户名
     * @return 用户信息
     */
    @ApiOperation(value = "查询用户信息")
    @GetMapping("/findByUserName")
    public UserInfoDto findByUserName(@RequestParam("userName") String userName) {
        return UserInfoMapper.INSTANCE.userInfoDto(userInfoService.findByUserName(userName));
    }

    /**
     * 为gateway提供用户查询方法，需要返回密码
     * @param userName 用户名
     * @return 用户信息
     */
    @ApiOperation(value = "Gateway服务查询用户信息")
    @GetMapping("/findByUserNameForGateway")
    public UserInfoDto findByUserNameForGateway(@RequestParam("userName") String userName) {
        return UserInfoMapper.INSTANCE.userInfoDtoForGateway(userInfoService.findByUserName(userName));
    }

    @ApiOperation(value = "新增用户")
    @PostMapping("/addUser")
    public Response<String> addUser(@RequestBody Request<UserInfoDto> request) {
        UserInfoEntity userInfoEntity = UserInfoMapper.INSTANCE.userInfoEntity(request.getBody());
        userInfoService.addUserInfo(userInfoEntity);
        return Response.ok();
    }

}
