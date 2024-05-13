package com.api.freemarket.account.handler;

import com.api.freemarket.account.model.PrincipalDetails;
import com.api.freemarket.account.model.RedisData;
import com.api.freemarket.account.service.RedisService;
import com.api.freemarket.jwt.JWTUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.Iterator;

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;

    private final RedisService redisService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();

        Long memberNo = principalDetails.getMemberNo();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        String accessToken = jwtUtil.createToken("access", memberNo, role, 60000L);
        String refreshToken = jwtUtil.createToken("refresh", memberNo, role, 86400000L);

        RedisData redisData = new RedisData();
        redisData.setMemberNo(memberNo);
        redisData.setRole(role);
        redisData.setRefreshToken(refreshToken);

        redisService.setValues(String.valueOf(memberNo), redisData, Duration.ofMillis(86400000L));

        response.setHeader("Authorization", "Bearer " + accessToken);
        response.addCookie(createCookie("refresh", refreshToken));
        response.setStatus(HttpStatus.OK.value());
    }

    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(604800);
        cookie.setHttpOnly(true);

        return cookie;
    }
}
