package com.api.freemarket.account.repository;

import com.api.freemarket.account.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByMemberId(String memberId);
}
