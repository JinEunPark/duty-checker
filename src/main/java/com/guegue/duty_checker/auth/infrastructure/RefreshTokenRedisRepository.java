package com.guegue.duty_checker.auth.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRedisRepository {

    private static final String TOKEN_KEY_PREFIX = "refresh:token:";
    private static final String PHONE_KEY_PREFIX = "refresh:phone:";
    private static final Duration TTL = Duration.ofDays(30);

    private final StringRedisTemplate redisTemplate;

    public void save(String token, String phone) {
        redisTemplate.opsForValue().set(TOKEN_KEY_PREFIX + token, phone, TTL);
        redisTemplate.opsForValue().set(PHONE_KEY_PREFIX + phone, token, TTL);
    }

    public Optional<String> findPhoneByToken(String token) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(TOKEN_KEY_PREFIX + token));
    }

    public void deleteByToken(String token) {
        Optional<String> phone = findPhoneByToken(token);
        redisTemplate.delete(TOKEN_KEY_PREFIX + token);
        phone.ifPresent(p -> redisTemplate.delete(PHONE_KEY_PREFIX + p));
    }

    public void deleteByPhone(String phone) {
        String token = redisTemplate.opsForValue().get(PHONE_KEY_PREFIX + phone);
        redisTemplate.delete(PHONE_KEY_PREFIX + phone);
        if (token != null) {
            redisTemplate.delete(TOKEN_KEY_PREFIX + token);
        }
    }
}
