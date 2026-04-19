package com.guegue.duty_checker.notification.service;

import com.guegue.duty_checker.checkin.service.CheckInService;
import com.guegue.duty_checker.connection.domain.Connection;
import com.guegue.duty_checker.connection.domain.ConnectionStatus;
import com.guegue.duty_checker.connection.repository.ConnectionRepository;
import com.guegue.duty_checker.notification.domain.NotificationLog;
import com.guegue.duty_checker.notification.infrastructure.FcmProvider;
import com.guegue.duty_checker.notification.repository.NotificationLogRepository;
import com.guegue.duty_checker.user.domain.User;
import com.guegue.duty_checker.user.service.UserFcmTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final ConnectionRepository connectionRepository;
    private final CheckInService checkInService;
    private final NotificationLogRepository notificationLogRepository;
    private final FcmProvider fcmProvider;
    private final UserFcmTokenService userFcmTokenService;

    public void sendConnectionRequestAlert(User target, User requester) {
        List<String> tokens = userFcmTokenService.getTokensByUser(target);
        if (tokens.isEmpty()) {
            return;
        }
        for (String token : tokens) {
            fcmProvider.send(
                    token,
                    "연결 신청이 왔습니다",
                    requester.getPhone() + "님이 연결을 신청했습니다."
            );
        }
    }

    @Transactional
    public void sendMissingCheckInAlerts() {
        ZonedDateTime now = ZonedDateTime.now(KST);
        ZonedDateTime threshold = now.minusHours(24);
        LocalDate today = now.toLocalDate();

        List<Connection> connected = connectionRepository.findByStatus(ConnectionStatus.CONNECTED);

        for (Connection connection : connected) {
            User subject = connection.getSubject();
            User guardian = connection.getGuardian();

            List<String> guardianTokens = userFcmTokenService.getTokensByUser(guardian);
            if (guardianTokens.isEmpty()) {
                continue;
            }

            var latest = checkInService.getLatestCheckInBySubject(subject);

            boolean notCheckedIn24h = latest.getLatestCheckedAt() == null
                    || latest.getLatestCheckedAt().isBefore(threshold);

            if (!notCheckedIn24h) {
                continue;
            }

            if (notificationLogRepository.existsBySubjectAndGuardianAndNotifiedDate(subject, guardian, today)) {
                continue;
            }

            String displayName = connection.getGuardianGivenName() != null
                    ? connection.getGuardianGivenName()
                    : subject.getPhone();

            for (String token : guardianTokens) {
                fcmProvider.send(
                        token,
                        "안부 확인 알림",
                        displayName + "님이 24시간 동안 안부 확인을 하지 않았습니다."
                );
            }

            notificationLogRepository.save(NotificationLog.builder()
                    .subject(subject)
                    .guardian(guardian)
                    .notifiedDate(today)
                    .build());

            log.info("[Notification] subject={}, guardian={}", subject.getPhone(), guardian.getPhone());
        }
    }
}
