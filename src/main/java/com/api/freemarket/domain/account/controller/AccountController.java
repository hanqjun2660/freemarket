package com.api.freemarket.domain.account.controller;

import com.api.freemarket.common.CommonResponse;
import com.api.freemarket.common.jwt.JWTUtil;
import com.api.freemarket.domain.account.model.PrincipalDetails;
import com.api.freemarket.domain.account.model.RedisData;
import com.api.freemarket.domain.account.model.UserDTO;
import com.api.freemarket.domain.account.service.RedisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
@RequestMapping("/account")
public class AccountController {

    @Value("${spring.jwt.access-duration}")
    private Long accessDuration;

    @Value("${spring.jwt.refresh-duration}")
    private Long refreshDuration;

    private final AuthenticationManager authenticationManager;

    private final JWTUtil jwtUtil;

    private final RedisService redisService;

    @Operation(summary = "로그인", description = "일반 회원의 로그인 API, memberId/password만 입력")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공시 200코드 반환",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                    examples = @ExampleObject(value = "{\n  \"statusCode\": \"200\",\n  \"message\": \"\",\n  \"data\": \"{}\"\n}"))),
            @ApiResponse(responseCode = "500", description = "실패시 500코드 및 메세지 반환",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                    examples = @ExampleObject(value = "{\n  \"statusCode\": \"500\",\n  \"message\": \"아이디 혹은 패스워드가 잘못되었습니다.\",\n  \"data\": \"{}\"\n}")))
    })
    @Parameters(value = {
            @Parameter(name = "memberId", description = "일반 회원 ID"),
            @Parameter(name = "password", description = "비밀번호")
    })
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

    @Operation(summary = "토큰 재발급", description = "accessToken이 만료되었을때 Token 재발급을 위한 API, Cookie내 RefreshToken 필수")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공시 200코드 및 메세지 반환, Authorization Header -> accessToken / Cookie -> refreshToken 재발급",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                    examples = @ExampleObject(value = "{\n  \"statusCode\": \"200\",\n  \"message\": \"정상적으로 처리됨\",\n  \"data\": \"{}\"\n}"))),
            @ApiResponse(responseCode = "500", description = "실패시 500코드 및 메세지 반환",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                    examples = @ExampleObject(value = "{\n  \"statusCode\": \"500\",\n  \"message\": \"Refresh Token의 유효기간이 만료됨\",\n  \"data\": \"{}\"\n}")))
    })
    @Parameters(value = {
            @Parameter(name = "refresh", description = "쿠키내 RefreshToken", in = ParameterIn.COOKIE)
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

        RedisData updateData = new RedisData(userNo, role, refreshToken);

        redisService.setValues(String.valueOf(userNo), updateData, Duration.ofMillis(86400000L));

        // 응답
        response.setHeader("Authorization", "Bearer " + newAccessToken);
        response.addCookie(jwtUtil.createCookie("refresh", newRefreshToken));

        return CommonResponse.OK("정상적으로 처리됨");
    }
}
