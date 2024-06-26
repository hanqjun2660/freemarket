package com.api.freemarket.common.jwt;

import com.api.freemarket.domain.account.model.PrincipalDetails;
import com.api.freemarket.domain.account.model.UserDTO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

@RequiredArgsConstructor
@Slf4j
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // header에 담긴 토큰 가져오기 AccessToken은 Authorization Header에 있음
        String accessToken = null;
        if(request.getHeader("HTTP_AUTHORIZATION") != null) {
            accessToken = request.getHeader("HTTP_AUTHORIZATION");
        } else {
            accessToken = request.getHeader("HTTP_AUTORIZATION");
        }
        log.info("jwt filter Authorization header value: {}", accessToken);

        // 요청에 accessToken이 존재하는지 확인
        if(accessToken == null) {
            filterChain.doFilter(request, response); // 다음필터로 넘김
            return;
        }

        // accessToken 접두사 "Bearer "를 제거
        String originToken = accessToken.substring(7);

        // accessToken이 만료되었는지 확인
        if(jwtUtil.isExpired(originToken)) {
            PrintWriter writer = response.getWriter();
            writer.println("Access Token Expired");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());       // 401 상태코드
            return;
        }

        // 요청에 들어온 Token이 Access Token이 맞는지
        String category = jwtUtil.getCategory(originToken);
        if(!"access".equals(category)) {
            PrintWriter writer = response.getWriter();
            writer.println("Invalid Access Token");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());    // 401 상태코드
            return;
        }

        // Security Context Holder
        Long memberNo = jwtUtil.getUserNo(originToken);
        String role = jwtUtil.getRole(originToken);

        UserDTO userDTO = new UserDTO();
        userDTO.setMemberNo(memberNo);
        userDTO.setRole(role);

        PrincipalDetails principalDetails = new PrincipalDetails(userDTO);

        Authentication authentication = new UsernamePasswordAuthenticationToken(principalDetails, null, principalDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }


    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {

        String[] excludePath = {
                "/swagger-ui/index.html",
                "/swagger-ui/swagger-ui-standalone-preset.js",
                "/swagger-ui/swagger-initializer.js",
                "/swagger-ui/swagger-ui-bundle.js",
                "/swagger-ui/swagger-ui.css",
                "/swagger-ui/index.css",
                "/swagger-ui/favicon-32x32.png",
                "/swagger-ui/favicon-16x16.png",
                "/api-docs/json/swagger-config",
                "/api-docs/json"
        };
        String path = request.getRequestURI();
        return Arrays.stream(excludePath).anyMatch(path::startsWith);
    }
}
