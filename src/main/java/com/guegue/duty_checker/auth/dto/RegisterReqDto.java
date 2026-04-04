package com.guegue.duty_checker.auth.dto;

import com.guegue.duty_checker.user.domain.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class RegisterReqDto {
    @NotBlank @Pattern(regexp = "^01[0-9]{8,9}$", message = "전화번호 형식이 올바르지 않습니다")
    private String phone;
    @NotBlank(message = "비밀번호를 입력해주세요")
    private String password;
    @NotNull(message = "역할을 선택해주세요")
    private Role role;
}
