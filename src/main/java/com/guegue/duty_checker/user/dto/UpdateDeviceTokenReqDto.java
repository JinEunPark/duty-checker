package com.guegue.duty_checker.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateDeviceTokenReqDto {

    @NotBlank
    private String fcmToken;
}
