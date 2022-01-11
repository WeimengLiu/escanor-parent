package com.escanor.multidatasource.service.impl;

import com.escanor.multidatasource.entity.UserEntity;
import com.escanor.multidatasource.repository.UserRepository;
import com.escanor.multidatasource.service.UserService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    final UserRepository userRepository;
    Log log = LogFactory.getLog(UserServiceImpl.class);

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserEntity findById(Long id) {
        log.info("查询用户ID：" + id);
        return userRepository.findById(id).orElse(null);
    }
}
