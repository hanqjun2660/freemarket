package com.api.freemarket.account.repository;

import com.api.freemarket.account.Entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Role findByMemberNo(Long memberNo);
}
