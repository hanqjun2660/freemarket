package com.api.freemarket.domain.mail.controller;

import com.api.freemarket.common.CommonResponse;
import com.api.freemarket.config.swagger.SwaggerCommonDesc;
import com.api.freemarket.config.swagger.SwaggerMailDesc;
import com.api.freemarket.domain.account.service.RedisService;
import com.api.freemarket.domain.mail.model.MailDTO;
import com.api.freemarket.domain.mail.service.MailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Random;

@Tag(name="Mail", description = "인증 번호 메일발송 및 검증")
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/mail")
public class Mailcontroller {

    private final RedisService redisService;

    private final MailService mailService;

    @Value("${spring.mail.auth-code-expiration-millis}")
    private long authCodeExpireationMillis;

    @Operation(summary = "인증번호 메일 발송", description = "회원 인증용 메일발송 api")
    @ApiResponses(value = {
            @ApiResponse(responseCode = SwaggerCommonDesc.RESPONSE_SUCCESS_CODE, description = SwaggerMailDesc.SEND_MAIL_SUCCESS_DESC,
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerCommonDesc.RESPONSE_SUCCESS_DESC))),
            @ApiResponse(responseCode = SwaggerCommonDesc.RESPONSE_FAILED_CODE, description = SwaggerMailDesc.SEND_MAIL_FAILED_DESC,
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerCommonDesc.RESPONSE_FAILED_DESC)))
    })
    @RequestBody(content = @Content(examples = {@ExampleObject(description = SwaggerMailDesc.SEND_MAIL_EX_DESC, value = SwaggerMailDesc.SEND_MAIL_EX_VAL)}))
    @PostMapping("/send")
    public CommonResponse sendCodeToEmail(@RequestBody MailDTO mailDTO) {
        String title = "[인증번호] FreeMarket 이메일 인증번호 입니다.";
        String origincode = createCode();
        String authCode = "인증번호 : " + origincode;

        mailService.sendEmail(mailDTO.getToEmail(), title, authCode);

        try {
            redisService.setValues(mailDTO.getToEmail(), origincode, Duration.ofMillis(authCodeExpireationMillis));
        } catch (Exception e) {
            log.info("mail controller exception");
            e.printStackTrace();
        }
        return CommonResponse.OK("메일 발송 성공");
    }

    private String createCode() {
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
