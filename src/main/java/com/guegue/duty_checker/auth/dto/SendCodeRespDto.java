package com.guegue.duty_checker.auth.dto;

import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
public class SendCodeRespDto {

    private final ZonedDateTime expiredAt;

    public SendCodeRespDto(ZonedDateTime expiredAt) {
        this.expiredAt = expiredAt;
    }
}
