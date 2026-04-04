package com.guegue.duty_checker.connection.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateConnectionNameReqDto {

    @NotBlank
    @Size(max = 20)
    private String name;
}
