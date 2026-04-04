package com.guegue.duty_checker.notification.infrastructure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MockFcmProvider implements FcmProvider {

    @Override
    public void send(String fcmToken, String title, String body) {
        log.info("[MockFCM] token={}, title={}, body={}", fcmToken, title, body);
    }
}
