package com.guegue.duty_checker.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationService notificationService;

    @Scheduled(fixedDelay = 3600000)
    public void checkMissingCheckIns() {
        log.info("[Scheduler] 미확인 안부 알림 체크 시작");
        notificationService.sendMissingCheckInAlerts();
        log.info("[Scheduler] 미확인 안부 알림 체크 완료");
    }
}
