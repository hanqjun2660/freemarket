package com.api.freemarket.config;

import com.api.freemarket.domain.account.handler.CustomLogoutHandler;
import com.api.freemarket.domain.account.handler.CustomOAuth2SuccessHandler;
import com.api.freemarket.domain.account.service.CustomOAuth2UserService;
import com.api.freemarket.jwt.JWTFilter;
import com.api.freemarket.jwt.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    private final CustomLogoutHandler customLogoutHandler;

    private final CustomOAuth2UserService customOAuth2UserService;

    private final JWTUtil jwtUtil;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // cors
        http.cors(corsCustomizer -> corsCustomizer.configurationSource(corsConfiguration()));

        // csrf
        http.csrf(AbstractHttpConfigurer::disable);

        // h2 콘솔 보려면 이거 해라(안쓰면 지우자)
        http.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));

        // jwt filter
        http.addFilterBefore(new JWTFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);

        // formLogin
        http.formLogin(AbstractHttpConfigurer::disable);

        // oAuth2
        http.oauth2Login((oauth2) -> oauth2
                .successHandler(customOAuth2SuccessHandler)
                .userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig
                        .userService(customOAuth2UserService)));

        // Logout
        http.logout((auth) -> auth
                .logoutUrl("/logout")
                .logoutSuccessHandler(customLogoutHandler)
                .deleteCookies("refresh"));

        // 경로별 인가(여기도 필요한거 추가해서 쓰자)
        http.authorizeHttpRequests((auth) -> auth
                .requestMatchers("/logout").authenticated()
                .requestMatchers("/account/**").permitAll()
                .anyRequest().permitAll());

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

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, PasswordEncoder passwordEncoder, UserDetailsService userDetailsService) throws Exception {
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder);
        return builder.build();
    }
}
