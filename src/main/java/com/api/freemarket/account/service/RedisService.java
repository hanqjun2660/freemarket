package com.api.freemarket.account.service;

import com.api.freemarket.account.model.RedisData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class RedisService {

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

}
