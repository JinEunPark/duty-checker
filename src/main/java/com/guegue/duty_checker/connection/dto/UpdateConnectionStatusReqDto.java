package com.guegue.duty_checker.connection.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateConnectionStatusReqDto {

    @NotBlank
    private String status;
}
