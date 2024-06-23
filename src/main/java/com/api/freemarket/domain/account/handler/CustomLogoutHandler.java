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
import java.util.Enumeration;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutSuccessHandler {

    private final JWTUtil jwtUtil;

    private final RedisService redisService;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        try {
            // Authorization Header가 잘 들어오는지 확인하기 위해 추가
            Enumeration<String> headerNames = request.getHeaderNames();
            while(headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                log.info("Header: {} = {}", headerName, request.getHeader(headerName));
            }
            String accessToken = request.getHeader("Authorization");
            log.info("accessToken : {}", accessToken);

            if (accessToken == null || !accessToken.startsWith("Bearer ")) {
                log.error("Invalid or missing Authorization header");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or missing Authorization header");
                return;
            }

            String originToken = accessToken.substring(7);
            log.info("originToken : {}", originToken);
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
