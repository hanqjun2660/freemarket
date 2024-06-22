package com.api.freemarket.domain.account.handler;

import com.api.freemarket.domain.account.service.RedisService;
import com.api.freemarket.common.jwt.JWTUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutSuccessHandler {

    private final JWTUtil jwtUtil;

    private final RedisService redisService;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        try {
            String accessToken = request.getHeader("Authorization");

            String originToken = accessToken.substring(7);

            Long key = jwtUtil.getUserNo(originToken);

            if(!redisService.checkExistsKey(String.valueOf(key))) {
                log.info("redis내에 해당하는 key에 대한 정보가 존재하지 않음");
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                return;
            }

            redisService.deleteValues(String.valueOf(key));
            response.setStatus(HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            log.info("로그아웃 처리중 예외가 발생함");
            response.setStatus(HttpStatus.BAD_REQUEST.value());
        }
    }
}
