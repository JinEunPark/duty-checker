package com.guegue.duty_checker.auth.dto;

import lombok.Getter;

@Getter
public class RefreshTokenRespDto {
    private final String accessToken;
    private final String refreshToken;

    public RefreshTokenRespDto(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
