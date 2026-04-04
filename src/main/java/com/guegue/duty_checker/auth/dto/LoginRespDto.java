package com.guegue.duty_checker.auth.dto;

import com.guegue.duty_checker.user.domain.Role;
import com.guegue.duty_checker.user.domain.User;
import lombok.Getter;

@Getter
public class LoginRespDto {
    private final String accessToken;
    private final String refreshToken;
    private final UserInfo user;

    public LoginRespDto(String accessToken, String refreshToken, User user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.user = new UserInfo(user.getId(), user.getPhone(), user.getRole());
    }

    public record UserInfo(Long id, String phone, Role role) {}
}
