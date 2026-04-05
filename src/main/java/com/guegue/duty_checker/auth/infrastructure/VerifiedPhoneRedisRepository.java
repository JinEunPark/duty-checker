package com.guegue.duty_checker.auth.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class VerifiedPhoneRedisRepository {

    private static final String KEY_PREFIX = "verified:phone:";
    private static final Duration TTL = Duration.ofMinutes(10);

    private final StringRedisTemplate redisTemplate;

    public void save(String phone) {
        redisTemplate.opsForValue().set(KEY_PREFIX + phone, "true", TTL);
    }

    public boolean isVerified(String phone) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + phone));
    }

    public void delete(String phone) {
        redisTemplate.delete(KEY_PREFIX + phone);
    }
}
