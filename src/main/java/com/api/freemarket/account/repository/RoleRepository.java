package com.api.freemarket.account.repository;

import com.api.freemarket.account.Entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
    String findByMemberNo(Long memberNo);
}
