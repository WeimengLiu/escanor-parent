package com.escanor.multidatasource.controller;

import com.escanor.multidatasource.common.ContextHolder;
import com.escanor.multidatasource.entity.UserEntity;
import com.escanor.multidatasource.service.UserService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping("/user/{code}/{id}")
    public UserEntity findById(@PathVariable(name = "code") String bankCode, @PathVariable(name = "id") Long id) {
        ContextHolder.setCode(bankCode);
        return userService.findById(id);
    }
}
