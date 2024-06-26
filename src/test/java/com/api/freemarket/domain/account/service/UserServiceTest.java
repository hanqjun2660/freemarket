package com.api.freemarket.domain.account.service;

import com.api.freemarket.domain.account.entity.QUser;
import com.api.freemarket.domain.account.entity.User;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
public class UserServiceTest {

    @Autowired
    EntityManager entityManager;

    @Test
    @Transactional
    void selectUserTest(){
        User user = new User();
        user.setMemberId("1234qwer");
        user.setPassword("1234");
        user.setName("유호준");
        user.setNickname("유호준");
        user.setEmail("1234@gmail.com");
        user.setPhone("01022223333");

        entityManager.persist(user);

        JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(entityManager);
        QUser qUser = QUser.user;

        User fetchedUser = jpaQueryFactory.selectFrom(qUser)
                .where(qUser.memberId.eq("1234qwer"))
                .fetchOne();

        // Then: 조회된 User가 존재하며, memberId가 "1234qwer"인지 확인
        assertThat(fetchedUser).isNotNull();
        assertThat(fetchedUser.getMemberId()).isEqualTo("1234qwer");
    }

}
