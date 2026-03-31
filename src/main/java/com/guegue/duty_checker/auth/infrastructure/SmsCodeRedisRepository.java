package com.guegue.duty_checker.auth.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SmsCodeRedisRepository {

    private static final String CODE_KEY = "sms:code:";
    private static final String ATTEMPTS_KEY = "sms:attempts:";
    private static final String COOLDOWN_KEY = "sms:cooldown:";
    private static final Duration CODE_TTL = Duration.ofMinutes(5);
    private static final Duration COOLDOWN_TTL = Duration.ofSeconds(60);
    private static final int MAX_ATTEMPTS = 3;

    private final StringRedisTemplate redisTemplate;

    public void saveCode(String phone, String code) {
        redisTemplate.opsForValue().set(CODE_KEY + phone, code, CODE_TTL);
        redisTemplate.delete(ATTEMPTS_KEY + phone);
        redisTemplate.opsForValue().set(COOLDOWN_KEY + phone, "1", COOLDOWN_TTL);
    }

    public Optional<String> findCode(String phone) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(CODE_KEY + phone));
    }

    public boolean isOnCooldown(String phone) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(COOLDOWN_KEY + phone));
    }

    public Long getRemainingCooldownSeconds(String phone) {
        Duration ttl = redisTemplate.getExpire(COOLDOWN_KEY + phone, java.util.concurrent.TimeUnit.SECONDS) > 0
                ? Duration.ofSeconds(redisTemplate.getExpire(COOLDOWN_KEY + phone, java.util.concurrent.TimeUnit.SECONDS))
                : Duration.ZERO;
        return ttl.getSeconds();
    }

    /** 오입력 횟수 증가. 3회 초과 시 코드 삭제 후 true 반환 */
    public boolean incrementAttemptsAndCheckExceeded(String phone) {
        Long attempts = redisTemplate.opsForValue().increment(ATTEMPTS_KEY + phone);
        redisTemplate.expire(ATTEMPTS_KEY + phone, CODE_TTL);
        if (attempts != null && attempts >= MAX_ATTEMPTS) {
            redisTemplate.delete(CODE_KEY + phone);
            redisTemplate.delete(ATTEMPTS_KEY + phone);
            return true;
        }
        return false;
    }

    public void deleteCode(String phone) {
        redisTemplate.delete(CODE_KEY + phone);
        redisTemplate.delete(ATTEMPTS_KEY + phone);
    }
}
