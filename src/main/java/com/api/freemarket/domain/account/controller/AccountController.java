package com.api.freemarket.domain.account.controller;

import com.api.freemarket.common.CommonResponse;
import com.api.freemarket.common.CommonResponseCode;
import com.api.freemarket.common.email.EmailUtil;
import com.api.freemarket.common.jwt.JWTUtil;
import com.api.freemarket.common.validation.ValidationGroups;
import com.api.freemarket.config.swagger.SwaggerAccountDesc;
import com.api.freemarket.config.swagger.SwaggerCommonDesc;
import com.api.freemarket.domain.account.entity.User;
import com.api.freemarket.domain.account.model.*;
import com.api.freemarket.domain.account.service.RedisService;
import com.api.freemarket.domain.account.service.UserService;
import com.api.freemarket.domain.mail.service.MailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;

@Tag(name="Account", description = "계정 관련 API")
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/account")
public class AccountController {

    @Value("${spring.jwt.access-duration}")
    private Long accessDuration;

    @Value("${spring.jwt.refresh-duration}")
    private Long refreshDuration;

    @Value("${spring.mail.auth-code-expiration-millis}")
    private long authCodeExpireationMillis;

    private final PasswordEncoder passwordEncoder;

    private final JWTUtil jwtUtil;

    private final EmailUtil emailUtil;

    private final RedisService redisService;

    private final UserService userService;

    private final MailService mailService;

    @Operation(summary = "로그인", description = SwaggerAccountDesc.NORMAL_USER_LOGIN_DESC)
    @ApiResponses(value = {
            @ApiResponse(responseCode = SwaggerCommonDesc.RESPONSE_SUCCESS_CODE, description = SwaggerAccountDesc.NORMAL_USER_LOGIN_SUCCESS_DESC,
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                    examples = @ExampleObject(value = SwaggerAccountDesc.NORMAL_USER_LOGIN_SUCCESS_EX_VAL))),
            @ApiResponse(responseCode = SwaggerCommonDesc.RESPONSE_FAILED_CODE, description = SwaggerAccountDesc.NORMAL_USER_LOGIN_FAILED_DESC,
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                    examples = @ExampleObject(value = SwaggerCommonDesc.RESPONSE_FAILED_DESC)))
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = {@ExampleObject(value = SwaggerAccountDesc.NORMAL_USER_LOGIN_EX_VAL)}))
    @PostMapping("/login")
    public CommonResponse login(@RequestBody @Validated({ValidationGroups.loginValidation.class}) UserDTO userDTO, HttpServletResponse response) {

        PrincipalDetails principalDetails = (PrincipalDetails) userService.loadUserByUsername(userDTO.getMemberId());

        if(!passwordEncoder.matches(userDTO.getPassword(), principalDetails.getPassword())) {
            return CommonResponse.ERROR("비밀번호가 일치하지 않습니다.");
        }

        Long memberNo = principalDetails.getMemberNo();

        Collection<? extends GrantedAuthority> authorities = principalDetails.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        redisService.tokenWithInsertRedis(memberNo, role, response);

        if("Y".equalsIgnoreCase(principalDetails.getTempPassStatus())) {
            Map<String, String> dataMap = new HashMap<>();
            dataMap.put("tempPassStatus", principalDetails.getTempPassStatus());
            return CommonResponse.OK(CommonResponseCode.REDIRECTION, "임시 비밀번호를 발급 받았습니다.\n비밀번호 변경 페이지로 이동합니다.", dataMap);
        }

        return CommonResponse.OK(null);
    }

    @Operation(summary = "토큰 재발급", description = SwaggerAccountDesc.TOKEN_REISSUE_DESC)
    @ApiResponses(value = {
            @ApiResponse(responseCode = SwaggerCommonDesc.RESPONSE_SUCCESS_CODE, description = SwaggerAccountDesc.NORMAL_USER_LOGIN_SUCCESS_DESC,
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerCommonDesc.RESPONSE_SUCCESS_DESC))),
            @ApiResponse(responseCode = SwaggerCommonDesc.RESPONSE_FAILED_CODE, description = SwaggerAccountDesc.NORMAL_USER_LOGIN_FAILED_DESC,
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerCommonDesc.RESPONSE_FAILED_DESC)))
    })
    @PostMapping("/reissue")
    public CommonResponse reissue(HttpServletRequest request, HttpServletResponse response) {

        String refreshToken = null;

        Cookie[] cookies = request.getCookies();

        try {
            for(Cookie cookie : cookies) {
                if("refresh".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                }
            }
        } catch (NullPointerException e) {
            CommonResponse.ERROR("Cookie가 존재하지 않음");
        }

        // cookie에 실제로 토큰이 있는지
        if(refreshToken == null) {
            return CommonResponse.ERROR("Cookie내 Refresh Token이 존재하지 않음");
        }

        // refreshToken이 맞는지 확인
        if(!"refresh".equals(jwtUtil.getCategory(refreshToken))) {
            return CommonResponse.ERROR("요청에 존재하는 Token이 Refresh Token이 아님");
        }

        // refreshToken이 유효한지 확인
        if(jwtUtil.isExpired(refreshToken)) {
            return CommonResponse.ERROR("Refresh Token의 유효기간이 만료됨");
        }

        Long userNo = jwtUtil.getUserNo(refreshToken);
        String role = jwtUtil.getRole(refreshToken);

        // redis에 refreshToken이 존재하는지 확인
        Optional<RedisData> redisData = Optional.ofNullable(redisService.getValues(String.valueOf(userNo)));

        if(!redisData.isPresent()) {
            return CommonResponse.ERROR("Refresh Token에 대한 정보가 존재하지 않음");
        }

        String storedRefreshToken = redisData.map(RedisData::getRefreshToken).orElse(null);
        log.info(storedRefreshToken);

        // redis에 저장된 refreshToken과 요청으로 들어온 refreshToken을 비교
        if(!refreshToken.trim().equals(storedRefreshToken.trim())) {
            return CommonResponse.ERROR("저장된 Token의 정보와 요청에 존재하는 Token의 정보가 다름");
        }

        String newAccessToken = jwtUtil.createToken("access", userNo, role, accessDuration);
        String newRefreshToken = jwtUtil.createToken("refresh", userNo, role, refreshDuration);

        RedisData updateData = new RedisData(userNo, role, newRefreshToken);

        redisService.setValues(String.valueOf(userNo), updateData, Duration.ofMillis(refreshDuration));

        // 응답
        response.setHeader("Authorization", "Bearer " + newAccessToken);
        response.addCookie(jwtUtil.createCookie("refresh", newRefreshToken));

        return CommonResponse.OK("정상적으로 처리됨");
    }

    @Operation(summary = "회원가입", description = SwaggerAccountDesc.JOIN_DESC)
    @ApiResponses(value = {
            @ApiResponse(responseCode = SwaggerCommonDesc.RESPONSE_SUCCESS_CODE, description = SwaggerAccountDesc.JOIN_SUCCESS_DESC,
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerCommonDesc.RESPONSE_SUCCESS_DESC))),
            @ApiResponse(responseCode = SwaggerCommonDesc.RESPONSE_FAILED_CODE, description = SwaggerAccountDesc.JOIN_FAILED_DESC,
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerCommonDesc.RESPONSE_FAILED_DESC)))
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = {@ExampleObject(value = SwaggerAccountDesc.JOIN_EX_VAL)}))
    @PostMapping("/join")
    public CommonResponse join(@RequestBody UserAndAddressDTO userAndAddressDTO) {

        boolean existMemberId = userService.existsByMemberId(userAndAddressDTO.getUserDTO().getMemberId());

        if(existMemberId) {
            return CommonResponse.ERROR("해당 아이디를 사용하는 사용자가 존재합니다.", false);
        }

        String encodePassword = passwordEncoder.encode(userAndAddressDTO.getUserDTO().getPassword());
        userAndAddressDTO.getUserDTO().setPassword(encodePassword);

        // 사용자 정보 DB 저장
        User joinUser = userService.joinUser(userAndAddressDTO.getUserDTO(), userAndAddressDTO.getAddressDTO());
        if(ObjectUtils.isEmpty(joinUser)) {
            CommonResponse.ERROR("회원가입 실패");
        }

        return CommonResponse.OK(null);
    }

    @Operation(summary = "별명 중복 체크", description = SwaggerAccountDesc.CHECK_NICKNAME_DESC)
    @ApiResponses(value = {
            @ApiResponse(responseCode = SwaggerCommonDesc.RESPONSE_SUCCESS_CODE, description = SwaggerAccountDesc.CHECK_NICKNAME_SUCCESS_DESC,
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerCommonDesc.RESPONSE_SUCCESS_DESC))),
            @ApiResponse(responseCode = SwaggerCommonDesc.RESPONSE_FAILED_CODE, description = SwaggerAccountDesc.CHECK_NICKNAME_FAILED_DESC,
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerCommonDesc.RESPONSE_FAILED_DESC)))
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = {@ExampleObject(value = SwaggerAccountDesc.CHECK_NICKNAME_EX_VAL)}))
    @PostMapping("/check-nickname")
    public CommonResponse checkNickname(@RequestBody @Validated({ValidationGroups.NicknameValidation.class}) UserDTO userDTO) {

        boolean exists = userService.existsByNickname(userDTO.getNickname());
        if(exists) {
            return CommonResponse.ERROR("해당 별명을 사용하는 사용자가 존재합니다.");
        } else {
            return CommonResponse.OK("사용 가능한 별명입니다.");
        }
    }

    @Operation(summary = "아이디 중복 체크", description = SwaggerAccountDesc.MEMBER_ID_DESC)
    @ApiResponses(value = {
            @ApiResponse(responseCode = SwaggerCommonDesc.RESPONSE_SUCCESS_CODE, description = SwaggerAccountDesc.MEMBER_ID_SUCCESS_DESC,
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerCommonDesc.RESPONSE_SUCCESS_DESC))),
            @ApiResponse(responseCode = SwaggerCommonDesc.RESPONSE_FAILED_CODE, description = SwaggerAccountDesc.MEMBER_ID_FAILED_DESC,
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerCommonDesc.RESPONSE_FAILED_DESC)))
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = {@ExampleObject(value = SwaggerAccountDesc.MEMBER_ID_EX_VAL)}))
    @PostMapping("/check-id")
    public CommonResponse checkId(@RequestBody @Validated({ValidationGroups.memberIdValidation.class}) UserDTO userDTO) {

        boolean exists = userService.existsByMemberId(userDTO.getMemberId());
        if(exists) {
            return CommonResponse.ERROR("해당 아이디를 사용하는 사용자가 존재합니다.");
        } else {
            return CommonResponse.OK("사용 가능한 아이디입니다.");
        }
    }

    @Operation(summary = "소셜 유저 회원가입", description = SwaggerAccountDesc.SOCIAL_USER_JOIN_DESC)
    @ApiResponses(value = {
            @ApiResponse(responseCode = SwaggerCommonDesc.RESPONSE_SUCCESS_CODE, description = SwaggerAccountDesc.SOCIAL_USER_JOIN_SUCCESS_DESC,
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerCommonDesc.RESPONSE_SUCCESS_DESC))),
            @ApiResponse(responseCode = SwaggerCommonDesc.RESPONSE_FAILED_CODE, description = SwaggerAccountDesc.SOCIAL_USER_JOIN_FAILED_DESC,
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerCommonDesc.RESPONSE_FAILED_DESC)))
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = {@ExampleObject(value = SwaggerAccountDesc.SOCIAL_USER_JOIN_EX_VAL)}))
    @PostMapping("/social-user-join")
    public CommonResponse socialUserJoin(@RequestBody @Validated({ValidationGroups.requestSocialUserRegistValidation.class}) UserDTO userDTO) {
        try {
            PrincipalDetails principalDetails = redisService.getSoicalTempData(userDTO.getEmail());

            log.info("soical temp data: {}", principalDetails.toString());

            userDTO.setMemberId(principalDetails.getMemberId());
            userDTO.setProvider(principalDetails.getProvider());
            userDTO.setName(principalDetails.getName());
            userDTO.setProfileImg(principalDetails.getProfileImage());

            log.info("settings userDTO: {}", userDTO.toString());

            User joinUser = userService.joinSocialUser(userDTO);

            log.info("social join principal: {}", principalDetails.getAttributes());
            log.info("social join userDTO: {}", userDTO.toString());

            if(!ObjectUtils.isEmpty(joinUser)) {
                return CommonResponse.OK("회원가입이 정상 처리 되었습니다.",null);
            }

            return CommonResponse.ERROR("회원가입이 정상적으로 처리되지 않았습니다.",null);
        } catch (NullPointerException e) {
            return CommonResponse.ERROR("정상적인 접근이 아닙니다.", null);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return CommonResponse.ERROR("내부 서버 오류", null);
        }
    }

    @Operation(summary = "비밀번호 찾기용 이메일 인증번호 발송", description = SwaggerAccountDesc.FIND_PASSWORD_CERT_DESC)
    @ApiResponses(value = {
            @ApiResponse(responseCode = SwaggerCommonDesc.RESPONSE_SUCCESS_CODE, description = SwaggerAccountDesc.FIND_PASSWORD_CERT_SUCCESS_DESC,
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerAccountDesc.FIND_PASSWORD_CERT_SUCCESS_EX_VAL))),
            @ApiResponse(responseCode = SwaggerCommonDesc.RESPONSE_FAILED_CODE, description = SwaggerAccountDesc.FIND_PASSWORD_CERT_FAILED_DESC,
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerCommonDesc.RESPONSE_FAILED_DESC)))
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = {@ExampleObject(value = SwaggerAccountDesc.FIND_PASSWORD_CERT_EX_VAL)}))
    @PostMapping("/find-password/send-cert-num")
    public CommonResponse findPassword(@RequestBody @Validated({ValidationGroups.findPasswordValidation.class}) FindIdAndPwRequest request) {

        String title = "[인증번호] 나플나플에서 인증번호를 전달드립니다.";
        String bodyTitle = "안녕하세요. 나플나플 인증번호 입니다.";
        String bodyText = "아래 인증번호를 입력하여 진행해주세요.";
        String originCode = emailUtil.createCode();
        String certCode = "인증번호: " + originCode;

        request.setEmailTitle(title);
        request.setEmailText(certCode);

        // 인증번호 메일 발송 후 확인용으로 redis에 저장
        userService.existMemberIdAndEmail(request);

        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("bodyTitle", bodyTitle);
        dataMap.put("bodyText", bodyText);
        dataMap.put("certNum", certCode);

        mailService.sendTemplateEmail(title, request.getEmail(), dataMap);
        redisService.setValues(request.getEmail(), originCode, Duration.ofMillis(authCodeExpireationMillis));

        Map<String, Long> response = new HashMap<>();
        response.put("duration", authCodeExpireationMillis);

        return CommonResponse.OK("메일 발송 성공", response);
    }

    @Operation(summary = "임시 비밀번호 발급", description = SwaggerAccountDesc.TEMP_PASSWORD_ISSUED_DESC)
    @ApiResponses(value = {
            @ApiResponse(responseCode = SwaggerCommonDesc.RESPONSE_SUCCESS_CODE, description = SwaggerAccountDesc.TEMP_PASSWORD_ISSUED_SUCCESS_DESC,
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerCommonDesc.RESPONSE_SUCCESS_DESC))),
            @ApiResponse(responseCode = SwaggerCommonDesc.RESPONSE_FAILED_CODE, description = SwaggerAccountDesc.TEMP_PASSWORD_ISSUED_FAILED_DESC,
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerCommonDesc.RESPONSE_FAILED_DESC)))
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = {@ExampleObject(value = SwaggerAccountDesc.TEMP_PASSWORD_ISSUED_EX_VAL)}))
    @PostMapping("/find-password/temp-password")
    public CommonResponse issuedTempPassword(@RequestBody @Validated({ValidationGroups.findPasswordValidation.class, ValidationGroups.requestTempPasswordValidation.class}) FindIdAndPwRequest request) {

        if(!"Y".equalsIgnoreCase(request.getVerify())) {
            return CommonResponse.ERROR("유효하지 않은 요청입니다.");
        }

        String title = "[임시 비밀번호] 나플나플에서 임시 비밀번호를 발송드립니다.";
        String bodyTitle = "안녕하세요. 임시 비밀번호를 전달드립니다.";
        String bodyText = "아래의 임시 비밀번호를 이용해 로그인 해주세요.\n 로그인 후 비밀번호를 꼭 변경해주세요.";
        String tempPassword = emailUtil.createTempPassword();

        userService.existMemberIdAndEmail(request);

        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("bodyTitle", bodyTitle);
        dataMap.put("bodyText", bodyText);
        dataMap.put("tempPassword", "임시 비밀번호 : " + tempPassword);

        mailService.sendTemplateEmail(title, request.getEmail(), dataMap);

        userService.tempChangePassword(request.getMemberId(), passwordEncoder.encode(tempPassword));

        return CommonResponse.OK("임시 비밀번호가 메일로 발송되었습니다.");
    }

    @Operation(summary = "아이디 찾기용 이메일 인증번호 발송", description = SwaggerAccountDesc.FIND_ID_CERT_DESC)
    @ApiResponses(value = {
            @ApiResponse(responseCode = SwaggerCommonDesc.RESPONSE_SUCCESS_CODE, description = SwaggerAccountDesc.FIND_ID_CERT_SUCCESS_DESC,
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerAccountDesc.FIND_ID_CERT_SUCCESS_EX_VAL))),
            @ApiResponse(responseCode = SwaggerCommonDesc.RESPONSE_FAILED_CODE, description = SwaggerAccountDesc.FIND_ID_CERT_FAILED_DESC,
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerCommonDesc.RESPONSE_FAILED_DESC)))
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = {@ExampleObject(value = SwaggerAccountDesc.FIND_ID_CERT_EX_VAL)}))
    @PostMapping("/find-id/send-cert-num")
    public CommonResponse findId(@RequestBody @Validated({ValidationGroups.findIdValidation.class}) FindIdAndPwRequest request) {

        String title = "[인증번호] 나플나플에서 인증번호를 전달드립니다.";
        String bodyTitle = "안녕하세요. 나플나플 인증번호 입니다.";
        String bodyText = "아래 인증번호를 입력하여 진행해주세요.";
        String certCode = emailUtil.createCode();

        request.setEmailTitle(title);
        request.setEmailText(certCode);

        // 유저가 있는지 확인
        userService.existsEmail(request);

        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("bodyTitle", bodyTitle);
        dataMap.put("bodyText", bodyText);
        dataMap.put("certNum", "인증번호: " + certCode);

        // 인증번호 메일 발송 후 확인용으로 redis에 저장
        mailService.sendTemplateEmail(title, request.getEmail(), dataMap);
        redisService.setValues(request.getEmail(), certCode, Duration.ofMillis(authCodeExpireationMillis));

        Map<String, Long> response = new HashMap<>();
        response.put("duration", authCodeExpireationMillis);

        return CommonResponse.OK("인증번호가 발송되었습니다.", response);
    }

    @Operation(summary = "아이디 찾기 정보(아이디/소셜로그인 정보)", description = SwaggerAccountDesc.FIND_ID_USER_INFO_DESC)
    @ApiResponses(value = {
            @ApiResponse(responseCode = SwaggerCommonDesc.RESPONSE_SUCCESS_CODE, description = SwaggerAccountDesc.FIND_ID_USER_INFO_SUCCESS_DESC,
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerAccountDesc.FIND_ID_USER_INFO_SUCCESS_EX_VAL))),
            @ApiResponse(responseCode = SwaggerCommonDesc.RESPONSE_FAILED_CODE, description = SwaggerAccountDesc.FIND_ID_USER_INFO_FAILED_DESC,
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerCommonDesc.RESPONSE_FAILED_DESC)))
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = {@ExampleObject(value = SwaggerAccountDesc.FIND_ID_USER_INFO_EX_VAL)}))
    @PostMapping("/find-id/user-info")
    public CommonResponse findIdUserInfo(@RequestBody @Validated({ValidationGroups.findIdValidation.class, ValidationGroups.requestFindIdValidation.class}) FindIdAndPwRequest request) {

        if(!"Y".equalsIgnoreCase(request.getVerify())) {
            return CommonResponse.ERROR("유효하지 않은 요청입니다.");
        }

        String email = request.getEmail();

        UserDTO userDTO = userService.findByEmail(email);

        Map<String, Object> data = new HashMap<>();

        if("site".equals(userDTO.getProvider())){    // 일반 회원의 경우
            data.put("memberID", replacingMiddleLettersOfMemberId(userDTO.getMemberId()));
        } else {                                // 소셜 로그인 경우
            switch (userDTO.getProvider()) {
                case "kakao": data.put("provider", "카카오");
                    break;
                case "naver": data.put("provider", "네이버");
                    break;
                case "google": data.put("provider", "구글");
                    break;
            }
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String registDate = dateFormat.format(userDTO.getJoinDate());

        data.put("registDate", registDate);

        return CommonResponse.OK("정상적으로 처리되었습니다.",data);
    }

    /**
     * 아이디 찾기시 중간 3글자 '*'로 치환하는 메서드
     * @param text 아이디
     * @return 치환된 아이디
     */
    public String replacingMiddleLettersOfMemberId(String text) {
        // 3글자 이하면 변환 안함
        if(text.length() < 3) {
            return text;
        }

        int mid = text.length() / 2;

        if(text.length() % 2 == 0) {        // 짝수인 경우
            return text.substring(0, mid - 1) + "***" + text.substring(mid + 2);
        } else {        // 홀수인 경우
            return text.substring(0, mid - 1) + "***" + text.substring(mid + 2);
        }
    }
}
