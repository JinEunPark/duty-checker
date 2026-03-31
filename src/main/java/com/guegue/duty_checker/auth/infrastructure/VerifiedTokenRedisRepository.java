package com.guegue.duty_checker.auth.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class VerifiedTokenRedisRepository {

    private static final String KEY = "verified:token:";
    private static final Duration TTL = Duration.ofMinutes(10);

    private final StringRedisTemplate redisTemplate;

    public void save(String token, String phone) {
        redisTemplate.opsForValue().set(KEY + token, phone, TTL);
    }

    public Optional<String> findPhoneByToken(String token) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(KEY + token));
    }

    /** 1회 사용 후 삭제 */
    public Optional<String> consumeToken(String token) {
        String phone = redisTemplate.opsForValue().get(KEY + token);
        if (phone != null) {
            redisTemplate.delete(KEY + token);
        }
        return Optional.ofNullable(phone);
    }
}
