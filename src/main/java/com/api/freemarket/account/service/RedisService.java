package com.api.freemarket.account.service;

import com.api.freemarket.account.model.RedisData;
import com.api.freemarket.jwt.JWTUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.Duration;

@Service
@Slf4j
@RequiredArgsConstructor
public class RedisService {

    @Value("${spring.jwt.access-duration}")
    private Long accessDuration;

    @Value("${spring.jwt.refresh-duration}")
    private Long refreshDuration;

    private final JWTUtil jwtUtil;

    private final RedisTemplate<String, Object> redisTemplate;

    // TTL 설정 X
    public void setValues(String key, RedisData data) {
        ValueOperations<String, Object> values = redisTemplate.opsForValue();
        values.set(key, data);
    }

    // refrshToken TTL 설정 O
    public void setValues(String key, RedisData data, Duration duration) {
        ValueOperations<String, Object> values = redisTemplate.opsForValue();
        values.set(key, data, duration);
    }

    // redis에 저장된 refreshToken 삭제
    public void deleteValues(String key) {
        redisTemplate.delete(key);
    }

    // redis에 저장된 refreshToken 조회
    public RedisData getValues(String key) {
        RedisData jsonValue = (RedisData) redisTemplate.opsForValue().get(key);

        if(ObjectUtils.isEmpty(jsonValue)) {
            return null;
        }

        return jsonValue;
    }

    public boolean checkExistsValue(String value) {
        return value.equals("false");
    }

    public boolean checkExistsKey(String key) {
        return redisTemplate.hasKey(key) != null;
    }

    /**
     * 토큰 생성 후 redis insert
     * @param memberNo
     * @param role
     * @param response
     */
    public void tokenWithInsertRedis(Long memberNo, String role, HttpServletResponse response) {

        String accessToken = jwtUtil.createToken("access", memberNo, role, accessDuration);
        String refreshToken = jwtUtil.createToken("refresh", memberNo, role, refreshDuration);

        RedisData redisData = new RedisData();
        redisData.setMemberNo(memberNo);
        redisData.setRole(role);
        redisData.setRefreshToken(refreshToken);

        setValues(String.valueOf(memberNo), redisData, Duration.ofMillis(refreshDuration));

        response.setHeader("Authorization", "Bearer " + accessToken);
        response.addCookie(jwtUtil.createCookie("refresh", refreshToken));
    }

}
