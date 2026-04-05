package com.guegue.duty_checker.auth.infrastructure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MockSmsProvider implements SmsProvider {

    @Override
    public void send(String phone, String code) {
        log.info("[MOCK SMS] {} 으로 인증코드 발송: {}", phone, code);
    }
}
