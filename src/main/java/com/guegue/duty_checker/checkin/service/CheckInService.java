package com.guegue.duty_checker.checkin.service;

import com.guegue.duty_checker.checkin.domain.CheckIn;
import com.guegue.duty_checker.checkin.dto.CreateCheckInRespDto;
import com.guegue.duty_checker.checkin.dto.GetLatestCheckInRespDto;
import com.guegue.duty_checker.checkin.repository.CheckInRepository;
import com.guegue.duty_checker.common.exception.BusinessException;
import com.guegue.duty_checker.common.exception.ErrorCode;
import com.guegue.duty_checker.user.domain.Role;
import com.guegue.duty_checker.user.domain.User;
import com.guegue.duty_checker.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CheckInService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final CheckInRepository checkInRepository;
    private final UserService userService;

    @Transactional
    public CreateCheckInRespDto createCheckIn(String phone) {
        User user = userService.getByPhone(phone);

        if (user.getRole() != Role.SUBJECT) {
            throw new BusinessException(ErrorCode.CHECK_IN_FORBIDDEN);
        }

        ZonedDateTime todayStart = LocalDate.now(KST).atStartOfDay(KST);
        ZonedDateTime todayEnd = todayStart.plusDays(1);

        if (checkInRepository.existsBySubjectAndCheckedAtBetween(user, todayStart, todayEnd)) {
            throw new BusinessException(ErrorCode.ALREADY_CHECKED_IN);
        }

        CheckIn checkIn = CheckIn.builder()
                .subject(user)
                .checkedAt(ZonedDateTime.now(KST))
                .build();
        checkInRepository.save(checkIn);

        return new CreateCheckInRespDto(checkIn);
    }

    @Transactional(readOnly = true)
    public GetLatestCheckInRespDto getLatestCheckIn(String phone) {
        User user = userService.getByPhone(phone);

        if (user.getRole() != Role.SUBJECT) {
            throw new BusinessException(ErrorCode.CHECK_IN_FORBIDDEN);
        }

        Optional<CheckIn> latest = checkInRepository.findTopBySubjectOrderByCheckedAtDesc(user);

        if (latest.isEmpty()) {
            return new GetLatestCheckInRespDto(null, false);
        }

        ZonedDateTime checkedAt = latest.get().getCheckedAt();
        ZonedDateTime todayStart = LocalDate.now(KST).atStartOfDay(KST);
        boolean isTodayChecked = !checkedAt.isBefore(todayStart);

        return new GetLatestCheckInRespDto(checkedAt, isTodayChecked);
    }

    @Transactional(readOnly = true)
    public GetLatestCheckInRespDto getLatestCheckInBySubject(User subject) {
        Optional<CheckIn> latest = checkInRepository.findTopBySubjectOrderByCheckedAtDesc(subject);

        if (latest.isEmpty()) {
            return new GetLatestCheckInRespDto(null, false);
        }

        ZonedDateTime checkedAt = latest.get().getCheckedAt();
        ZonedDateTime todayStart = LocalDate.now(KST).atStartOfDay(KST);
        boolean isTodayChecked = !checkedAt.isBefore(todayStart);

        return new GetLatestCheckInRespDto(checkedAt, isTodayChecked);
    }
}
