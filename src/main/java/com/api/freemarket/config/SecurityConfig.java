package com.api.freemarket.config;

import com.api.freemarket.account.handler.CustomOAuth2SuccessHandler;
import com.api.freemarket.account.service.CustomOAuth2UserService;
import com.api.freemarket.jwt.JWTFilter;
import com.api.freemarket.jwt.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2SuccessHandler customOAuth2SuccessHandler;

    private final CustomOAuth2UserService customOAuth2UserService;

    private final JWTUtil jwtUtil;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // cors
        http.cors(corsCustomizer -> corsCustomizer.configurationSource(corsConfiguration()));

        // csrf
        http.csrf((auth) -> auth.disable());

        // jwt filter
        http.addFilterBefore(new JWTFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);

        // formLogin
        http.formLogin((auth) -> auth.disable());

        // oAuth2
        http.oauth2Login((oauth2) -> oauth2
                .successHandler(customOAuth2SuccessHandler)
                .userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig
                        .userService(customOAuth2UserService)));

        // 경로별 인가(여기도 필요한거 추가해서 쓰자)
        http.authorizeHttpRequests((auth) -> auth
                .requestMatchers("/","/example").permitAll()
                .anyRequest().authenticated());

        // Session
        http.sessionManagement((session) -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfiguration() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));        // front 서버 주소 나오면 수정해야함
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(Arrays.asList("*"));        // 알맞게 수정해야함
        configuration.setAllowedMethods(Arrays.asList("*"));        // 알맞게 수정해야함
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
