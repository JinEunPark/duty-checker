package com.guegue.duty_checker.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class SendCodeReqDto {

    @NotBlank(message = "전화번호를 입력해주세요")
    @Pattern(regexp = "^01[0-9]{8,9}$", message = "올바른 전화번호 형식이 아닙니다")
    private String phoneNumber;
}
