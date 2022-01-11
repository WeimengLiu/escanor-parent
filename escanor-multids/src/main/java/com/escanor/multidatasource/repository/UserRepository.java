package com.escanor.multidatasource.repository;

import com.escanor.multidatasource.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
}
