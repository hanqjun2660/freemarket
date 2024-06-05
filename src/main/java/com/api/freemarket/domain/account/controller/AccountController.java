package com.api.freemarket.domain.account.controller;

import com.api.freemarket.common.CommonResponse;
import com.api.freemarket.common.jwt.JWTUtil;
import com.api.freemarket.config.swagger.SwaggerAccountDesc;
import com.api.freemarket.config.swagger.SwaggerCommonDesc;
import com.api.freemarket.domain.account.entity.User;
import com.api.freemarket.domain.account.enums.RoleName;
import com.api.freemarket.domain.account.model.PrincipalDetails;
import com.api.freemarket.domain.account.model.RedisData;
import com.api.freemarket.domain.account.model.UserDTO;
import com.api.freemarket.domain.account.service.RedisService;
import com.api.freemarket.domain.account.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

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

    private final AuthenticationManager authenticationManager;

    private final JWTUtil jwtUtil;

    private final RedisService redisService;

    private final UserService userService;

    @Operation(summary = "로그인", description = SwaggerAccountDesc.NORMAL_USER_LOGIN_DESC)
    @ApiResponses(value = {
            @ApiResponse(responseCode = SwaggerCommonDesc.RESPONSE_SUCCESS_CODE, description = SwaggerAccountDesc.NORMAL_USER_LOGIN_SUCCESS_DESC,
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                    examples = @ExampleObject(value = SwaggerCommonDesc.RESPONSE_SUCCESS_DESC))),
            @ApiResponse(responseCode = SwaggerCommonDesc.RESPONSE_FAILED_CODE, description = SwaggerAccountDesc.NORMAL_USER_LOGIN_FAILED_DESC,
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                    examples = @ExampleObject(value = SwaggerCommonDesc.RESPONSE_FAILED_DESC)))
    })
    @RequestBody(content = @Content(examples = {@ExampleObject(description = SwaggerAccountDesc.NORMAL_USER_LOGIN_EX_DESC, value = SwaggerAccountDesc.NORMAL_USER_LOGIN_EX_VAL)}))
    @PostMapping("/login")
    public CommonResponse login(@RequestBody UserDTO userDTO, HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userDTO.getMemberId(), userDTO.getPassword())
            );

            PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();

            Long memberNo = principalDetails.getMemberNo();

            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
            GrantedAuthority auth = iterator.next();
            String role = auth.getAuthority();

            redisService.tokenWithInsertRedis(memberNo, role, response);

            return CommonResponse.OK(null);

        } catch (BadCredentialsException e) {
            return CommonResponse.ERROR("아이디 혹은 패스워드가 잘못되었습니다.");
        }
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

    @Operation(summary = "추가 정보 입력(소셜 회원)", description = SwaggerAccountDesc.ADD_INFO_DESC)
    @ApiResponses(value = {
            @ApiResponse(responseCode = SwaggerCommonDesc.RESPONSE_SUCCESS_CODE, description = SwaggerAccountDesc.NORMAL_USER_LOGIN_SUCCESS_DESC,
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerCommonDesc.RESPONSE_SUCCESS_DESC))),
            @ApiResponse(responseCode = SwaggerCommonDesc.RESPONSE_FAILED_CODE, description = SwaggerAccountDesc.NORMAL_USER_LOGIN_FAILED_DESC,
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerCommonDesc.RESPONSE_FAILED_DESC)))
    })
    @RequestBody(content = @Content(examples = {@ExampleObject(description = SwaggerAccountDesc.ADD_INFO_EX_DESC, value = SwaggerAccountDesc.ADD_INFO_EX_VAL)}))
    @PostMapping("/add-info")
    public CommonResponse addInfo(@RequestBody UserDTO userDTO, HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        long memberNo = 0L;

        try {
            for(Cookie cookie : cookies) {
                if("memberNo".equals(cookie.getName())) {
                    memberNo = Long.parseLong(cookie.getValue());
                }
            }
        } catch (NullPointerException e) {
            CommonResponse.ERROR("Cookie가 존재하지 않음");
        }

        User updateUser = userService.insertByMemberNo(userDTO, memberNo);

        if(ObjectUtils.isEmpty(updateUser)) {
            return CommonResponse.ERROR("해당 회원이 존재하지 않음");
        }

        String role = String.valueOf(RoleName.ROLE_USER);

        redisService.tokenWithInsertRedis(memberNo, role, response);

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
    @RequestBody(content = @Content(examples = {@ExampleObject(description = SwaggerAccountDesc.JOIN_EX_DESC, value = SwaggerAccountDesc.JOIN_EX_VAL)}))
    @PostMapping("/join")
    public CommonResponse join(@RequestBody UserDTO userDTO, HttpServletResponse response) {

        // 클라이언트가 꼭 보내야만 하는 값
        // userDTO.memberId. password, name, nickname, email
        if(StringUtils.isEmpty(userDTO.getMemberId()) ||
                StringUtils.isEmpty(userDTO.getPassword()) ||
                StringUtils.isEmpty(userDTO.getName()) ||
                StringUtils.isEmpty(userDTO.getNickname()) ||
                StringUtils.isEmpty(userDTO.getEmail())) {

            return CommonResponse.ERROR("회원가입 내용을 모두 입력해주세요.");
        }

        // 사용자 정보 DB 저장
        User joinUser = userService.joinUser(userDTO);

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
    @RequestBody(content = @Content(examples = {@ExampleObject(description = SwaggerAccountDesc.CHECK_NICKNAME_EX_DESC, value = SwaggerAccountDesc.CHECK_NICKNAME_EX_VAL)}))
    @PostMapping("/check-nickname")
    public CommonResponse checkNickname(@RequestBody UserDTO userDTO, HttpServletResponse response) {

        // 값이 넘어왔는지 체크
        if(StringUtils.isEmpty(userDTO.getNickname())) {
            return CommonResponse.ERROR("별명을 입력해주세요.");
        }

        boolean exists = userService.existsByNickname(userDTO.getNickname());
        if(exists) {
            return CommonResponse.OK("해당 별명을 사용하는 사용자가 존재합니다.", exists);
        } else {
            return CommonResponse.OK("사용 가능한 별명입니다.", exists);
        }


    }


}
