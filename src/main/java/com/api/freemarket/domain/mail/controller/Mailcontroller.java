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
import jakarta.mail.internet.MimeMessage;
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
import java.util.HashMap;
import java.util.Map;

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

        String title = "[인증번호] 나플나플에서 인증번호를 전달드립니다.";
        String bodyTitle = "안녕하세요. 나플나플 인증번호 입니다.";
        String bodyText = "아래 인증번호를 입력하여 진행해주세요.";
        String toEmail = certNumberSendRequest.getToEmail();
        String origincode = emailUtil.createCode();
        String authCode = "인증번호 : " + origincode;

        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("bodyTitle", bodyTitle);
        dataMap.put("bodyText", bodyText);
        dataMap.put("certNum", authCode);

        mailService.sendTemplateEmail(title, toEmail, dataMap);

        try {
            redisService.setValues(toEmail, origincode, Duration.ofMillis(authCodeExpireationMillis));
        } catch (Exception e) {
            log.info("mail controller exception");
            e.printStackTrace();
        }
        return CommonResponse.OK("메일 발송 성공");
    }

    @Operation(summary = "인증번호 검증", description = SwaggerMailDesc.VALID_MAIL_DESC)
    @ApiResponses(value = {
            @ApiResponse(responseCode = SwaggerCommonDesc.RESPONSE_SUCCESS_CODE, description = SwaggerMailDesc.VALID_MAIL_SUCCESS_DESC,
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                    examples = @ExampleObject(value = SwaggerMailDesc.VALID_MAIL_SUCCESS_EX_VAL))),
            @ApiResponse(responseCode = SwaggerCommonDesc.RESPONSE_FAILED_CODE, description = SwaggerMailDesc.VALID_MAIL_FALIED_DESC,
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerMailDesc.VALID_MAIL_FALIED_EX_VAL)))
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = {@ExampleObject(description = SwaggerMailDesc.VALID_MAIL_NUMBER_EX_DESC, value = SwaggerMailDesc.VALID_MAIL_NUMBER_EX_VAL)}))
    @PostMapping("/valid-cert-num")
    public CommonResponse validCertNumer(@RequestBody @Valid VaildCertNumberRequest request) {

        String redisCertNo = "";
        Map<String, String> response = new HashMap<>();

        try {
            redisCertNo = redisService.getValuesForString(request.getEmail());
            if(redisCertNo == null) {
                response.put("verify", "N");
                return CommonResponse.ERROR("인증번호 발송 내역이 존재하지 않음", response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return CommonResponse.ERROR("서버 내부오류 발생");
        }

        if(!request.getCertNo().equals(redisCertNo)) {
            response.put("verify", "N");
            return CommonResponse.ERROR("인증번호가 일치하지 않습니다.", response);
        }

        response.put("verify", "Y");

        return CommonResponse.OK(null, response);
    }
}
