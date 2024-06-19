package com.api.freemarket.domain.mail.service;

import com.api.freemarket.domain.account.entity.User;
import com.api.freemarket.domain.account.model.FindPasswordRequest;
import com.api.freemarket.domain.account.repository.UserRepository;
import com.api.freemarket.domain.account.service.UserService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class MailServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    JavaMailSender emailSender;

    @InjectMocks
    MailService mailService;

    @InjectMocks
    UserService userService;

    @BeforeAll
    public static void setUp() {
        System.setProperty("user.timezone", "Asia/Seoul");
        System.setProperty("spring.profiles.active", "dev");
        System.setProperty("jasypt.encryptor.password", "jasyptPropEncodeKey");
    }

    @Test
    void 비밀번호_찾기_본인인증용_인증번호_발송_테스트_예외_발생() {
        // given
        FindPasswordRequest request = new FindPasswordRequest();
        request.setMemberId("rkdtjd56");
        request.setEmail("hanqjun2660@gmail.com");
        request.setEmailTitle("[인증번호] FreeMarket 이메일 인증번호 입니다.");
        request.setEmailText(createTempPassword());

        // UserRepository가 Optional.empty()를 반환하도록 Mocking
        when(userRepository.existsByMemberIdAndEmail(request.getMemberId(), request.getEmail()))
                .thenReturn(Optional.empty());

        // 예외가 발생하는지 테스트
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            userService.existMemberIdAndEmail(request);
        });

        assertEquals("해당 가입정보가 존재하지 않습니다.", exception.getMessage());
    }

    @Test
    void 비밀번호_찾기_본인인증용_인증번호_발송_테스트_메일_전송_실패() {
        // given
        FindPasswordRequest request = new FindPasswordRequest();
        request.setMemberId("rkdtjd56");
        request.setEmail("hanqjun2660@gmail.com");
        request.setEmailTitle("[인증번호] FreeMarket 이메일 인증번호 입니다.");
        request.setEmailText(createTempPassword());

        // UserRepository가 실제 사용자를 반환하도록 Mocking
        User user = new User();
        user.setMemberId("rkdtjd56");
        user.setEmail("hanqjun2660@gmail.com");
        when(userRepository.existsByMemberIdAndEmail(request.getMemberId(), request.getEmail()))
                .thenReturn(Optional.of(user));

        // 이메일 전송시 MailException이 발생하도록 Mocking
        doThrow(new MailException("메일 전송 실패") {}).when(emailSender).send(any(SimpleMailMessage.class));

        // 메일 전송 실패 시 예외가 발생하는지 테스트
        assertThrows(MailException.class, () -> userService.existMemberIdAndEmail(request));
    }

    @Test
    void 비밀번호_찾기_본인인증용_인증번호_발송_테스트_메일_전송_성공() {
        // given
        FindPasswordRequest request = new FindPasswordRequest();
        request.setMemberId("rkdtjd56");
        request.setEmail("hanqjun2660@gmail.com");
        request.setEmailTitle("[인증번호] FreeMarket 이메일 인증번호 입니다.");
        request.setEmailText(createTempPassword());

        // UserRepository가 실제 사용자를 반환하도록 Mocking
        User user = new User();
        user.setMemberId("rkdtjd56");
        user.setEmail("hanqjun2660@gmail.com");
        when(userRepository.existsByMemberIdAndEmail(request.getMemberId(), request.getEmail()))
                .thenReturn(Optional.of(user));

        // when & then
        assertDoesNotThrow(() -> userService.existMemberIdAndEmail(request));

        // then
        verify(emailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    private String createTempPassword() {
        int length = 10;
        String charSet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        try {
            Random random = SecureRandom.getInstanceStrong();
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < length; i++) {
                int index = random.nextInt(charSet.length());
                sb.append(charSet.charAt(index));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
