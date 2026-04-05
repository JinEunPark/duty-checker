package com.guegue.duty_checker.checkin.service;

import com.guegue.duty_checker.checkin.domain.CheckIn;
import com.guegue.duty_checker.checkin.repository.CheckInRepository;
import com.guegue.duty_checker.common.exception.BusinessException;
import com.guegue.duty_checker.common.exception.ErrorCode;
import com.guegue.duty_checker.user.domain.Role;
import com.guegue.duty_checker.user.domain.User;
import com.guegue.duty_checker.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CheckInServiceTest {

    @InjectMocks CheckInService checkInService;

    @Mock CheckInRepository checkInRepository;
    @Mock UserService userService;

    private User user(String phone, Role role) {
        return User.builder().phone(phone).password("pw").role(role).build();
    }

    private CheckIn checkInAt(User user, ZonedDateTime time) {
        CheckIn ci = CheckIn.builder().subject(user).checkedAt(time).build();
        return ci;
    }

    // ─── createCheckIn ─────────────────────────────────────────────────────

    @Test
    void createCheckIn_보호자역할_예외발생() {
        User guardian = user("01022222222", Role.GUARDIAN);
        given(userService.getByPhone("01022222222")).willReturn(guardian);

        assertThatThrownBy(() -> checkInService.createCheckIn("01022222222"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.CHECK_IN_FORBIDDEN);
    }

    @Test
    void createCheckIn_오늘이미체크인_예외발생() {
        User subject = user("01011111111", Role.SUBJECT);
        given(userService.getByPhone("01011111111")).willReturn(subject);
        given(checkInRepository.existsBySubjectAndCheckedAtBetween(eq(subject), any(), any())).willReturn(true);

        assertThatThrownBy(() -> checkInService.createCheckIn("01011111111"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ALREADY_CHECKED_IN);
    }

    @Test
    void createCheckIn_정상_체크인저장() {
        User subject = user("01011111111", Role.SUBJECT);
        given(userService.getByPhone("01011111111")).willReturn(subject);
        given(checkInRepository.existsBySubjectAndCheckedAtBetween(eq(subject), any(), any())).willReturn(false);

        var resp = checkInService.createCheckIn("01011111111");

        verify(checkInRepository).save(any(CheckIn.class));
        assertThat(resp.getCheckedAt()).isNotNull();
    }

    // ─── getLatestCheckIn ──────────────────────────────────────────────────

    @Test
    void getLatestCheckIn_보호자역할_예외발생() {
        User guardian = user("01022222222", Role.GUARDIAN);
        given(userService.getByPhone("01022222222")).willReturn(guardian);

        assertThatThrownBy(() -> checkInService.getLatestCheckIn("01022222222"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.CHECK_IN_FORBIDDEN);
    }

    @Test
    void getLatestCheckIn_체크인없음_false반환() {
        User subject = user("01011111111", Role.SUBJECT);
        given(userService.getByPhone("01011111111")).willReturn(subject);
        given(checkInRepository.findTopBySubjectOrderByCheckedAtDesc(subject)).willReturn(Optional.empty());

        var resp = checkInService.getLatestCheckIn("01011111111");

        assertThat(resp.getLatestCheckedAt()).isNull();
        assertThat(resp.isTodayChecked()).isFalse();
    }

    @Test
    void getLatestCheckIn_오늘체크인_true반환() {
        User subject = user("01011111111", Role.SUBJECT);
        ZonedDateTime now = ZonedDateTime.now();
        CheckIn ci = checkInAt(subject, now);
        given(userService.getByPhone("01011111111")).willReturn(subject);
        given(checkInRepository.findTopBySubjectOrderByCheckedAtDesc(subject)).willReturn(Optional.of(ci));

        var resp = checkInService.getLatestCheckIn("01011111111");

        assertThat(resp.isTodayChecked()).isTrue();
    }

    @Test
    void getLatestCheckIn_어제체크인_false반환() {
        User subject = user("01011111111", Role.SUBJECT);
        ZonedDateTime yesterday = ZonedDateTime.now().minusDays(1);
        CheckIn ci = checkInAt(subject, yesterday);
        given(userService.getByPhone("01011111111")).willReturn(subject);
        given(checkInRepository.findTopBySubjectOrderByCheckedAtDesc(subject)).willReturn(Optional.of(ci));

        var resp = checkInService.getLatestCheckIn("01011111111");

        assertThat(resp.isTodayChecked()).isFalse();
    }
}
