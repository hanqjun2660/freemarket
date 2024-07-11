package com.api.freemarket.domain.account.handler;

import com.api.freemarket.domain.account.model.PrincipalDetails;
import com.api.freemarket.domain.account.service.RedisService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final RedisService redisService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        HttpSession session = request.getSession();

        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();

        log.info("oAuth data: {}", principalDetails.getAttributes());

        if(ObjectUtils.isEmpty(principalDetails.getMemberNo())) {
            // 소셜로 회원가입 진행해야하는 경우
            session.setAttribute(principalDetails.PRINCIPAL_SESSION_KEY , principalDetails);
            response.setStatus(HttpStatus.FOUND.value());
            response.sendRedirect("http://localhost:3000");
            response.addCookie(new Cookie("email", principalDetails.getEmail()));
            return;
        }

        // 소셜로 회원가입이 되어있어 로그인 처리 해야하는 경우
        Long userNo = principalDetails.getMemberNo();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        redisService.tokenWithInsertRedis(userNo, role, response);

        response.setStatus(HttpStatus.OK.value());
    }

    public Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(60*10);
        cookie.setHttpOnly(true);

        return cookie;
    }
}
