package com.api.freemarket.account.controller;

import com.api.freemarket.account.model.PrincipalDetails;
import com.api.freemarket.account.model.RedisData;
import com.api.freemarket.account.model.UserDTO;
import com.api.freemarket.account.service.RedisService;
import com.api.freemarket.common.CommonResponse;
import com.api.freemarket.jwt.JWTUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/account")
public class AccountController {

    private final AuthenticationManager authenticationManager;

    private final JWTUtil jwtUtil;

    private final RedisService redisService;

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

        String newAccessToken = jwtUtil.createToken("access", userNo, role, 60000L);
        String newRefreshToken = jwtUtil.createToken("refresh", userNo, role, 86400000L);

        RedisData updateData = new RedisData(userNo, role, refreshToken);

        redisService.setValues(String.valueOf(userNo), updateData, Duration.ofMillis(86400000L));

        // 응답
        response.setHeader("Authorization", "Bearer " + newAccessToken);
        response.addCookie(createCookie("refresh", newRefreshToken));

        return CommonResponse.OK("정상적으로 처리됨");
    }

    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(604800);
        cookie.setHttpOnly(true);

        return cookie;
    }
}
