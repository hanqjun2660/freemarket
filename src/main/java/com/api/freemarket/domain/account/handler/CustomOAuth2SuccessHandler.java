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
import java.util.*;

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
            /*response.addCookie(new Cookie("email", principalDetails.getEmail()));*/

            // HttpServletRequest에 있는 헤더를 모두 출력
            Map<String, String> headers = new HashMap<>();

            Enumeration<String> headerNames = request.getHeaderNames();
            while(headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                String headerValue = request.getHeader(headerName);
                headers.put(headerName, headerValue);

                log.info(String.format("Header '%s' = %s", headerName, headerValue));
            }

            String origin = request.getHeader("origin");
            String cookieDomain = "devsj.site";

            if(origin != null && origin.contains("localhost")) {
                cookieDomain = "localhost";
            }

            log.info("origin: {}", origin);
            log.info("cookieDomain: {}", cookieDomain);

            // 쿠키 설정 - sendRedirect 이전에 설정해야 합니다.
            Cookie emailCookie = new Cookie("email", principalDetails.getEmail());
            emailCookie.setDomain(cookieDomain);  // 공통 도메인 설정
            emailCookie.setPath("/");  // 쿠키의 유효 경로 설정
            emailCookie.setHttpOnly(false);  // JavaScript에서 쿠키 접근 가능 여부
            emailCookie.setSecure(true);  // SameSite=None을 사용하려면 Secure도 true로 설정해야 함
            emailCookie.setMaxAge(60 * 60);  // 쿠키 유효 기간 설정 (1시간)

            // 도메인 설정을 제거하여 현재 호스트에 쿠키를 설정
            response.addCookie(emailCookie);

            // SameSite=None 속성을 추가하기 위해 헤더를 수동으로 설정
            String cookieHeader = String.format(
                    "email=%s; Max-Age=%d; Path=%s; Domain=%s; Secure; SameSite=None",
                    principalDetails.getEmail(),
                    60 * 60,
                    "/",
                    cookieDomain
            );
            response.addHeader("Set-Cookie", cookieHeader);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"status\": \"success\"}");
            response.setStatus(HttpStatus.FOUND.value());

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
