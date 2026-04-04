package com.guegue.duty_checker.connection.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AddConnectionReqDto {

    @NotBlank
    @Pattern(regexp = "^\\d{11}$", message = "전화번호는 숫자 11자리여야 합니다")
    private String guardianPhone;

    @Size(max = 20)
    private String name;
}
