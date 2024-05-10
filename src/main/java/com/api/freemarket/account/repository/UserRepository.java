package com.api.freemarket.account.repository;

import com.api.freemarket.account.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    UserEntity findByMemberId(String memberId);
}
