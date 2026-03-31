package com.guegue.duty_checker.auth.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class SendCodeRespDto {

    private final String phoneNumber;
    private final LocalDateTime expiresAt;

    public SendCodeRespDto(String phoneNumber, LocalDateTime expiresAt) {
        this.phoneNumber = phoneNumber;
        this.expiresAt = expiresAt;
    }
}
