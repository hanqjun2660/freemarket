package com.api.freemarket.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;

@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // header에 담긴 토큰 가져오기 AccessToken은 Authorization Header에 있음
        String accessToken = request.getHeader("Authorization");

        // 요청에 accessToken이 존재하는지 확인
        if(accessToken == null) {
            filterChain.doFilter(request, response); // 다음필터로 넘김
        }

        // accessToken 접두사 "Bearer "를 제거
        String originToken = accessToken.substring(7);

        // accessToken이 만료되었는지 확인
        if(jwtUtil.isExpired(originToken)) {
            PrintWriter writer = response.getWriter();
            writer.println("Access Token Expired");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());       // 401 상태코드
        }

        // 요청에 들어온 Token이 Access Token이 맞는지
        String category = jwtUtil.getCategory(originToken);
        if(!"access".equals(category)) {
            PrintWriter writer = response.getWriter();
            writer.println("Invalid Access Token");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());    // 401 상태코드
        }

        // Security Context Holder
        Long memberNo = jwtUtil.getUserNo(originToken);
        String role = jwtUtil.getRole(originToken);
    }
}
