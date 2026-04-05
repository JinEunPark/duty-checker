package com.guegue.duty_checker.auth.service;

import com.guegue.duty_checker.auth.dto.*;
import com.guegue.duty_checker.auth.infrastructure.RefreshTokenRedisRepository;
import com.guegue.duty_checker.common.config.JwtProvider;
import com.guegue.duty_checker.common.exception.BusinessException;
import com.guegue.duty_checker.common.exception.ErrorCode;
import com.guegue.duty_checker.connection.service.ConnectionService;
import com.guegue.duty_checker.user.domain.User;
import com.guegue.duty_checker.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final RefreshTokenRedisRepository refreshTokenRedisRepository;
    private final JwtProvider jwtProvider;
    private final UserService userService;
    private final ConnectionService connectionService;
    private final PasswordEncoder passwordEncoder;

    public RegisterRespDto register(RegisterReqDto reqDto) {
        String phone = reqDto.getPhone();

        if (userService.existsByPhone(phone)) {
            throw new BusinessException(ErrorCode.ALREADY_REGISTERED);
        }

        User user = User.builder()
                .phone(phone)
                .password(passwordEncoder.encode(reqDto.getPassword()))
                .role(reqDto.getRole())
                .build();
        userService.save(user);
        connectionService.activatePendingConnections(phone, user);

        return new RegisterRespDto(user);
    }

    public LoginRespDto login(LoginReqDto reqDto) {
        String phone = reqDto.getPhone();

        User user = userService.getByPhone(phone);

        if (!passwordEncoder.matches(reqDto.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        refreshTokenRedisRepository.deleteByPhone(phone);

        String accessToken = jwtProvider.generateAccessToken(phone);
        String refreshToken = jwtProvider.generateRefreshToken(phone);
        refreshTokenRedisRepository.save(refreshToken, phone);

        return new LoginRespDto(accessToken, refreshToken, user);
    }

    public void logout(String phone) {
        refreshTokenRedisRepository.deleteByPhone(phone);
        userService.clearFcmToken(phone);
    }

    public RefreshTokenRespDto refresh(RefreshTokenReqDto reqDto) {
        String oldRefreshToken = reqDto.getRefreshToken();

        String phone = refreshTokenRedisRepository.findPhoneByToken(oldRefreshToken)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));

        refreshTokenRedisRepository.deleteByToken(oldRefreshToken);

        String newAccessToken = jwtProvider.generateAccessToken(phone);
        String newRefreshToken = jwtProvider.generateRefreshToken(phone);
        refreshTokenRedisRepository.save(newRefreshToken, phone);

        return new RefreshTokenRespDto(newAccessToken, newRefreshToken);
    }
}
