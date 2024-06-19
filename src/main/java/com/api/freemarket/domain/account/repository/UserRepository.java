package com.api.freemarket.domain.account.repository;

import com.api.freemarket.domain.account.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByMemberId(String memberId);

    User findByMemberNo(long memberNo);

    boolean existsByNickname(String nickname);

    boolean existsByMemberId(String memberId);

    Optional<User> existsByMemberIdAndEmail(String memberId, String email);
}
