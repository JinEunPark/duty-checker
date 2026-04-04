package com.guegue.duty_checker.checkin.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.ZonedDateTime;

@Getter
@RequiredArgsConstructor
public class GetLatestCheckInRespDto {

    private final ZonedDateTime latestCheckedAt;
    private final boolean isTodayChecked;
}
