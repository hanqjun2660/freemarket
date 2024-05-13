package com.api.freemarket.account.controller;

import com.api.freemarket.account.model.RedisData;
import com.api.freemarket.account.service.RedisService;
import com.api.freemarket.jwt.JWTUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Optional;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ReissueController {

    private final JWTUtil jwtUtil;

    private final RedisService redisService;

    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {

        String refreshToken = null;
        Cookie[] cookies = request.getCookies();
        for(Cookie cookie : cookies) {
            if("refresh".equals(cookie.getName())) {
                refreshToken = cookie.getValue();
            }
        }

        // cookie에 실제로 토큰이 있는지
        if(refreshToken == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // refreshToken이 맞는지 확인
        if("refresh".equals(jwtUtil.getCategory(refreshToken))) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // refreshToken이 유효한지 확인
        if(jwtUtil.isExpired(refreshToken)) {
            return new ResponseEntity<>(HttpStatus.LOCKED);
        }

        Long userNo = jwtUtil.getUserNo(refreshToken);
        String role = jwtUtil.getRole(refreshToken);

        // redis에 refreshToken이 존재하는지 확인
        Optional<RedisData> redisData = Optional.ofNullable(redisService.getValues(String.valueOf(userNo)));

        if(!redisData.isPresent()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // redis에 저장된 refreshToken과 요청으로 들어온 refreshToken을 비교
        if(!refreshToken.equals(redisData.get().getRefreshToken())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        String newAccessToken = jwtUtil.createToken("access", userNo, role, 60000L);
        String newRefreshToken = jwtUtil.createToken("refresh", userNo, role, 86400000L);

        RedisData updateData = new RedisData(userNo, role, refreshToken);

        redisService.setValues(String.valueOf(userNo), updateData, Duration.ofMillis(86400000L));

        // 응답
        response.setHeader("Authorization", "Bearer " + newAccessToken);
        response.addCookie(createCookie("refresh", newRefreshToken));

        return new ResponseEntity<>(HttpStatus.OK);
    }

    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(604800);
        cookie.setHttpOnly(true);

        return cookie;
    }
}
