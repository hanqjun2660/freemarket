package com.api.freemarket.domain.account.controller;

import com.api.freemarket.common.CommonResponse;
import com.api.freemarket.common.jwt.JWTUtil;
import com.api.freemarket.config.swagger.SwaggerAccountDesc;
import com.api.freemarket.config.swagger.SwaggerCommonDesc;
import com.api.freemarket.domain.account.service.RedisService;
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
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name="Account", description = "계정 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class LogoutController {

    private final JWTUtil jwtUtil;

    private final RedisService redisService;

    /*@Operation(summary = "로그아웃", description = SwaggerAccountDesc.LOGOUT_USER_DESC)
    @ApiResponses(value = {
            @ApiResponse(responseCode = SwaggerCommonDesc.RESPONSE_SUCCESS_CODE, description = SwaggerAccountDesc.LOGOUT_USER_SUCCESS_DESC,
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerCommonDesc.RESPONSE_SUCCESS_DESC))),
            @ApiResponse(responseCode = SwaggerCommonDesc.RESPONSE_FAILED_CODE, description = SwaggerAccountDesc.LOGOUT_USER_FAILED_DESC,
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerCommonDesc.RESPONSE_FAILED_DESC)))
    })
    @PostMapping("/logout")
    public CommonResponse logout(HttpServletRequest request, HttpServletResponse response) {

        String accessToken = request.getHeader("Authorization").substring(7);

        if(accessToken == null) {
            return CommonResponse.ERROR("로그인된 사용자가 아닙니다.");
        }

        String redisRefreshTokenKey = String.valueOf(jwtUtil.getUserNo(accessToken));

        if(redisRefreshTokenKey == null) {
            return CommonResponse.ERROR("로그아웃된 사용자 입니다.");
        }

        redisService.deleteValues(redisRefreshTokenKey);

        // 클라이언트 측 Cookie내 refreshToken을 null로 바꿔줌
        Cookie cookie = new Cookie("refresh", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");

        response.addCookie(cookie);
        response.setHeader("Authorization", null);
        return CommonResponse.OK("정상적으로 로그아웃 처리 되었습니다.");
    }*/
}
