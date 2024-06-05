package com.api.freemarket.domain.mail.controller;

import com.api.freemarket.common.CommonResponse;
import com.api.freemarket.domain.account.service.RedisService;
import com.api.freemarket.domain.mail.model.MailDTO;
import com.api.freemarket.domain.mail.service.MailService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Random;

@Tag(name="Account", description = "계정 관련 API")
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/mail")
public class Mailcontroller {

    private final RedisService redisService;

    private final MailService mailService;

    @Value("${spring.mail.auth-code-expiration-millis}")
    private long authCodeExpireationMillis;

    @PostMapping("/send")
    public CommonResponse sendCodeToEmail(@RequestBody MailDTO mailDTO) {
        String title = "[인증번호] FreeMarket 이메일 인증번호 입니다.";
        String origincode = createcode();
        String authCode = "인증번호 : " + origincode;
        mailService.sendEmail(mailDTO.getToEmail(), title, authCode);
        log.info("mail controller exception1");
        redisService.setValues(mailDTO.getToEmail(), origincode, Duration.ofMillis(authCodeExpireationMillis));
        log.info("mail controller exception2");
        return CommonResponse.OK("메일 발송 성공");
    }

    private String createcode() {
        int length = 8;
        try {
            Random random = SecureRandom.getInstanceStrong();
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < length; i++) {
                sb.append(random.nextInt(10));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            log.debug("createCode exception");
            throw new RuntimeException(e);
        }
    }
}
