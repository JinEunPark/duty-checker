package com.guegue.duty_checker.auth.dto;

import com.guegue.duty_checker.user.domain.Role;
import com.guegue.duty_checker.user.domain.User;
import lombok.Getter;

@Getter
public class RegisterRespDto {
    private final Long id;
    private final String phone;
    private final Role role;

    public RegisterRespDto(User user) {
        this.id = user.getId();
        this.phone = user.getPhone();
        this.role = user.getRole();
    }
}
