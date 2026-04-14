package com.guegue.duty_checker.notification.service;

import com.guegue.duty_checker.checkin.dto.GetLatestCheckInRespDto;
import com.guegue.duty_checker.checkin.service.CheckInService;
import com.guegue.duty_checker.connection.domain.Connection;
import com.guegue.duty_checker.connection.domain.ConnectionStatus;
import com.guegue.duty_checker.connection.repository.ConnectionRepository;
import com.guegue.duty_checker.notification.infrastructure.FcmProvider;
import com.guegue.duty_checker.notification.repository.NotificationLogRepository;
import com.guegue.duty_checker.user.domain.Role;
import com.guegue.duty_checker.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.ZonedDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @InjectMocks NotificationService notificationService;

    @Mock ConnectionRepository connectionRepository;
    @Mock CheckInService checkInService;
    @Mock NotificationLogRepository notificationLogRepository;
    @Mock FcmProvider fcmProvider;

    private User subject(String phone) {
        return User.builder().phone(phone).password("pw").role(Role.SUBJECT).build();
    }

    private User guardianWithToken(String phone, String fcmToken) {
        User u = User.builder().phone(phone).password("pw").role(Role.GUARDIAN).build();
        u.updateFcmToken(fcmToken);
        return u;
    }

    private User guardianNoToken(String phone) {
        return User.builder().phone(phone).password("pw").role(Role.GUARDIAN).build();
    }

    private Connection connected(User subject, User guardian) {
        return Connection.builder()
                .subject(subject)
                .guardian(guardian)
                .guardianPhone(guardian.getPhone())
                .requester(subject)
                .status(ConnectionStatus.CONNECTED)
                .build();
    }

    // ─── sendConnectionRequestAlert ───────────────────────────────────────

    @Test
    void sendConnectionRequestAlert_FCM토큰있음_알림발송() {
        User target = guardianWithToken("01022222222", "token-xyz");
        User requester = subject("01011111111");

        notificationService.sendConnectionRequestAlert(target, requester);

        verify(fcmProvider).send("token-xyz", "연결 신청이 왔습니다", "01011111111님이 연결을 신청했습니다.");
    }

    @Test
    void sendConnectionRequestAlert_FCM토큰없음_알림스킵() {
        User target = guardianNoToken("01022222222");
        User requester = subject("01011111111");

        notificationService.sendConnectionRequestAlert(target, requester);

        verify(fcmProvider, never()).send(anyString(), anyString(), anyString());
    }

    // ─── sendMissingCheckInAlerts ──────────────────────────────────────────

    @Test
    void sendMissingCheckInAlerts_FCM토큰없는보호자_알림스킵() {
        User subject = subject("01011111111");
        User guardian = guardianNoToken("01022222222");
        given(connectionRepository.findByStatus(ConnectionStatus.CONNECTED))
                .willReturn(List.of(connected(subject, guardian)));

        notificationService.sendMissingCheckInAlerts();

        verify(fcmProvider, never()).send(anyString(), anyString(), anyString());
    }

    @Test
    void sendMissingCheckInAlerts_24시간이내체크인_알림스킵() {
        User subject = subject("01011111111");
        User guardian = guardianWithToken("01022222222", "token-abc");
        ZonedDateTime recentCheckIn = ZonedDateTime.now().minusHours(1);
        given(connectionRepository.findByStatus(ConnectionStatus.CONNECTED))
                .willReturn(List.of(connected(subject, guardian)));
        given(checkInService.getLatestCheckInBySubject(subject))
                .willReturn(new GetLatestCheckInRespDto(recentCheckIn, true));

        notificationService.sendMissingCheckInAlerts();

        verify(fcmProvider, never()).send(anyString(), anyString(), anyString());
    }

    @Test
    void sendMissingCheckInAlerts_오늘이미알림발송됨_알림스킵() {
        User subject = subject("01011111111");
        User guardian = guardianWithToken("01022222222", "token-abc");
        ZonedDateTime old = ZonedDateTime.now().minusHours(25);
        given(connectionRepository.findByStatus(ConnectionStatus.CONNECTED))
                .willReturn(List.of(connected(subject, guardian)));
        given(checkInService.getLatestCheckInBySubject(subject))
                .willReturn(new GetLatestCheckInRespDto(old, false));
        given(notificationLogRepository.existsBySubjectAndGuardianAndNotifiedDate(any(), any(), any()))
                .willReturn(true);

        notificationService.sendMissingCheckInAlerts();

        verify(fcmProvider, never()).send(anyString(), anyString(), anyString());
    }

    @Test
    void sendMissingCheckInAlerts_정상_알림발송및로그저장() {
        User subject = subject("01011111111");
        User guardian = guardianWithToken("01022222222", "token-abc");
        ZonedDateTime old = ZonedDateTime.now().minusHours(25);
        given(connectionRepository.findByStatus(ConnectionStatus.CONNECTED))
                .willReturn(List.of(connected(subject, guardian)));
        given(checkInService.getLatestCheckInBySubject(subject))
                .willReturn(new GetLatestCheckInRespDto(old, false));
        given(notificationLogRepository.existsBySubjectAndGuardianAndNotifiedDate(any(), any(), any()))
                .willReturn(false);

        notificationService.sendMissingCheckInAlerts();

        verify(fcmProvider).send(anyString(), anyString(), anyString());
        verify(notificationLogRepository).save(any());
    }
}
