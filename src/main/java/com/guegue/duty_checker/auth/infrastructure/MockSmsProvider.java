package com.guegue.duty_checker.auth.infrastructure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MockSmsProvider implements SmsProvider {

    @Override
    public void send(String phoneNumber, String code) {
        log.info("[MockSMS] 인증코드 발송 → 수신번호: {}, 코드: {}", phoneNumber, code);
    }
}
