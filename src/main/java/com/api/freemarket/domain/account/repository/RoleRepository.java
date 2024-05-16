package com.api.freemarket.domain.account.repository;

import com.api.freemarket.domain.account.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Role findByMemberNo(Long memberNo);
}
