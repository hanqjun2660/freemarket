package com.api.freemarket.domain.account.service;

import com.api.freemarket.domain.account.model.PrincipalDetails;
import com.api.freemarket.domain.account.model.RedisData;
import com.api.freemarket.common.jwt.JWTUtil;
import com.api.freemarket.domain.account.model.UserDTO;
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

    // 이메일 인증번호 저장용
    public void setValues(String key, String value, Duration duration) {
        ValueOperations<String, Object> values = redisTemplate.opsForValue();
        values.set(key, value, duration);
    }

    // 소셜회원 임시 회원 정보 저장용
    public void setValues(String key, UserDTO userDTO, Duration duration) {
        ValueOperations<String, Object> values = redisTemplate.opsForValue();
        values.set(key, userDTO, duration);
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

    // redis에 저장된 소셜 회원 임시정보 조회
    public UserDTO getSoicalTempData(String key) {
        UserDTO jsonValue = (UserDTO) redisTemplate.opsForValue().get(key);

        if(ObjectUtils.isEmpty(jsonValue)) {
            return null;
        }

        return jsonValue;
    }

    public String getValuesForString(String key) {
        return (String) redisTemplate.opsForValue().get(key);
    }

    public boolean checkExistsValue(String value) {
        return value.equals("false");
    }

    public boolean checkExistsKey(String key) {
        return redisTemplate.hasKey(key);
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
