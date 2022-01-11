package com.escanor.multidatasource.service;

import com.escanor.multidatasource.entity.UserEntity;

public interface UserService {
    UserEntity findById(Long id);
}
