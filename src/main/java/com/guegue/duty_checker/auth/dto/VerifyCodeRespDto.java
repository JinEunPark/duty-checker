package com.guegue.duty_checker.auth.dto;

import lombok.Getter;

@Getter
public class VerifyCodeRespDto {

    private final String accessToken;

    public VerifyCodeRespDto(String accessToken) {
        this.accessToken = accessToken;
    }
}
