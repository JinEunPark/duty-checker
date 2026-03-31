package com.guegue.duty_checker.auth.dto;

import lombok.Getter;

@Getter
public class VerifyCodeRespDto {

    private final String verifiedToken;

    public VerifyCodeRespDto(String verifiedToken) {
        this.verifiedToken = verifiedToken;
    }
}
