package com.guegue.duty_checker.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class RefreshTokenReqDto {
    @NotBlank(message = "refreshToken을 입력해주세요")
    private String refreshToken;
}
