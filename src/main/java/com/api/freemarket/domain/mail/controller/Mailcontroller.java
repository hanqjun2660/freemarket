package com.api.freemarket.domain.mail.controller;

import com.api.freemarket.common.CommonResponse;
import com.api.freemarket.common.email.EmailUtil;
import com.api.freemarket.config.swagger.SwaggerCommonDesc;
import com.api.freemarket.config.swagger.SwaggerMailDesc;
import com.api.freemarket.domain.account.service.RedisService;
import com.api.freemarket.domain.mail.model.CertNumberSendRequest;
import com.api.freemarket.domain.mail.model.VaildCertNumberRequest;
import com.api.freemarket.domain.mail.service.MailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@Tag(name="Mail", description = "인증 번호 메일발송 및 검증")
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/mail")
public class Mailcontroller {

    private final EmailUtil emailUtil;

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
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = {@ExampleObject(description = SwaggerMailDesc.SEND_MAIL_EX_DESC, value = SwaggerMailDesc.SEND_MAIL_EX_VAL)}))
    @PostMapping("/send")
    public CommonResponse sendCodeToEmail(@RequestBody @Valid CertNumberSendRequest certNumberSendRequest) {
        String title = "[인증번호] FreeMarket 이메일 인증번호 입니다.";
        String origincode = emailUtil.createCode();
        String authCode = "인증번호 : " + origincode;

        SimpleMailMessage emailForm = emailUtil.createEmailForm(certNumberSendRequest.getToEmail(), title, "인증번호 : " + authCode);

        mailService.sendEmail(emailForm);

        try {
            redisService.setValues(certNumberSendRequest.getToEmail(), origincode, Duration.ofMillis(authCodeExpireationMillis));
        } catch (Exception e) {
            log.info("mail controller exception");
            e.printStackTrace();
        }
        return CommonResponse.OK("메일 발송 성공");
    }

    @Operation(summary = "인증번호 검증", description = "사용자가 입력한 인증번호와 발송된 인증번호 상호 검증 api")
    @ApiResponses(value = {
            @ApiResponse(responseCode = SwaggerCommonDesc.RESPONSE_SUCCESS_CODE, description = SwaggerMailDesc.VALID_MAIL_SUCCESS_DESC,
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                    examples = @ExampleObject(value = SwaggerCommonDesc.RESPONSE_SUCCESS_DESC))),
            @ApiResponse(responseCode = SwaggerCommonDesc.RESPONSE_FAILED_CODE, description = SwaggerMailDesc.VALID_MAIL_FALIED_DESC,
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerCommonDesc.RESPONSE_FAILED_DESC)))
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = {@ExampleObject(description = SwaggerMailDesc.VALID_MAIL_NUMBER_EX_DESC, value = SwaggerMailDesc.VALID_MAIL_NUMBER_EX_VAL)}))
    @PostMapping("/valid-cert-num")
    public CommonResponse validCertNumer(@RequestBody @Valid VaildCertNumberRequest request) {

        String redisCertNo = "";

        try {
            redisCertNo = redisService.getValuesForString(request.getEmail());
        } catch (Exception e) {
            e.printStackTrace();
            return CommonResponse.ERROR("인증번호 발송 내역이 존재하지 않음");
        }

        if(!request.getCertNo().equals(redisCertNo)) {
            return CommonResponse.ERROR("인증번호가 일치하지 않습니다.");
        }

        return CommonResponse.OK(null);
    }
}
