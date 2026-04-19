package com.guegue.duty_checker.auth.service;

import com.guegue.duty_checker.auth.dto.*;
import com.guegue.duty_checker.auth.infrastructure.RefreshTokenRedisRepository;
import com.guegue.duty_checker.auth.infrastructure.SmsCodeRedisRepository;
import com.guegue.duty_checker.auth.infrastructure.SmsProvider;
import com.guegue.duty_checker.auth.infrastructure.VerifiedPhoneRedisRepository;
import com.guegue.duty_checker.checkin.service.CheckInService;
import com.guegue.duty_checker.common.config.JwtProvider;
import com.guegue.duty_checker.common.exception.BusinessException;
import com.guegue.duty_checker.common.exception.ErrorCode;
import com.guegue.duty_checker.connection.service.ConnectionService;
import com.guegue.duty_checker.user.domain.User;
import com.guegue.duty_checker.user.service.UserFcmTokenService;
import com.guegue.duty_checker.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final SmsCodeRedisRepository smsCodeRedisRepository;
    private final VerifiedPhoneRedisRepository verifiedPhoneRedisRepository;
    private final RefreshTokenRedisRepository refreshTokenRedisRepository;
    private final SmsProvider smsProvider;
    private final JwtProvider jwtProvider;
    private final UserService userService;
    private final UserFcmTokenService userFcmTokenService;
    private final ConnectionService connectionService;
    private final CheckInService checkInService;
    private final PasswordEncoder passwordEncoder;

    public SendCodeRespDto sendCode(SendCodeReqDto reqDto) {
        String phone = reqDto.getPhone();

        if (smsCodeRedisRepository.isOnCooldown(phone)) {
            long remaining = smsCodeRedisRepository.getRemainingCooldownSeconds(phone);
            throw new BusinessException(ErrorCode.AUTH_SEND_CODE_COOLDOWN,
                    String.format(ErrorCode.AUTH_SEND_CODE_COOLDOWN.getMessage(), remaining));
        }

        String code = generateCode();
        smsCodeRedisRepository.saveCode(phone, code);
        smsProvider.send(phone, code);

        return new SendCodeRespDto(ZonedDateTime.now(KST).plusMinutes(5));
    }

    public void verifyCode(VerifyCodeReqDto reqDto) {
        // SMS 발송 미구현으로 인해 코드 검증 없이 항상 인증 성공 처리
        verifiedPhoneRedisRepository.save(reqDto.getPhone());
    }

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
        userService.findByPhone(phone).ifPresent(userFcmTokenService::deleteAllByUser);
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

    public CheckPhoneRespDto checkPhone(String phone) {
        return new CheckPhoneRespDto(userService.existsByPhone(phone));
    }

    @Transactional
    public void changePassword(ChangePasswordReqDto reqDto) {
        String phone = reqDto.getPhone();
        validatePhoneVerified(phone);
        User user = userService.getByPhone(phone);
        user.updatePassword(passwordEncoder.encode(reqDto.getNewPassword()));
        verifiedPhoneRedisRepository.delete(phone);
    }

    private void validatePhoneVerified(String phone) {
        if (!verifiedPhoneRedisRepository.isVerified(phone)) {
            throw new BusinessException(ErrorCode.PHONE_NOT_VERIFIED);
        }
    }

    @Transactional
    public void withdraw(String phone) {
        User user = userService.getByPhone(phone);
        refreshTokenRedisRepository.deleteByPhone(phone);
        connectionService.deleteAllByUser(user);
        checkInService.deleteAllByUser(user);
        userService.deleteUser(phone);
    }

    private String generateCode() {
        return String.format("%06d", new Random().nextInt(1_000_000));
    }
}
