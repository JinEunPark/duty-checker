package com.guegue.duty_checker.notification.infrastructure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

// FirebaseCloudMessagingProvider(@Primary)가 항상 우선 적용됨.
// 이 빈은 테스트에서 @MockBean / @SpyBean으로 대체하거나, 직접 주입이 필요한 경우를 위한 fallback 구현체이다.
@Slf4j
@Component
public class MockFcmProvider implements FcmProvider {

    @Override
    public void send(String fcmToken, String title, String body) {
        log.info("[MockFCM] token={}, title={}, body={}", fcmToken, title, body);
    }
}
