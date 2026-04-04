package com.guegue.duty_checker.checkin.dto;

import com.guegue.duty_checker.checkin.domain.CheckIn;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
public class CreateCheckInRespDto {

    private final Long id;
    private final ZonedDateTime checkedAt;
    private final String status;

    public CreateCheckInRespDto(CheckIn checkIn) {
        this.id = checkIn.getId();
        this.checkedAt = checkIn.getCheckedAt();
        this.status = "CHECKED";
    }
}
