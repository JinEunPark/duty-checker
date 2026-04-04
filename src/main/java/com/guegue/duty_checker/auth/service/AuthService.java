package com.guegue.duty_checker.auth.service;

import com.guegue.duty_checker.auth.dto.*;
import com.guegue.duty_checker.auth.infrastructure.RefreshTokenRedisRepository;
import com.guegue.duty_checker.auth.infrastructure.SmsCodeRedisRepository;
import com.guegue.duty_checker.auth.infrastructure.SmsProvider;
import com.guegue.duty_checker.auth.infrastructure.VerifiedPhoneRedisRepository;
import com.guegue.duty_checker.common.config.JwtProvider;
import com.guegue.duty_checker.common.exception.BusinessException;
import com.guegue.duty_checker.common.exception.ErrorCode;
import com.guegue.duty_checker.user.domain.User;
import com.guegue.duty_checker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
    private final UserRepository userRepository;
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
        String phone = reqDto.getPhone();

        String storedCode = smsCodeRedisRepository.findCode(phone)
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_CODE_EXPIRED));

        if (!storedCode.equals(reqDto.getVerificationCode())) {
            boolean exceeded = smsCodeRedisRepository.incrementAttemptsAndCheckExceeded(phone);
            if (exceeded) {
                throw new BusinessException(ErrorCode.AUTH_CODE_ATTEMPTS_EXCEEDED);
            }
            throw new BusinessException(ErrorCode.AUTH_CODE_MISMATCH);
        }

        smsCodeRedisRepository.deleteCode(phone);
        verifiedPhoneRedisRepository.save(phone);
    }

    public RegisterRespDto register(RegisterReqDto reqDto) {
        String phone = reqDto.getPhone();

        if (!verifiedPhoneRedisRepository.isVerified(phone)) {
            throw new BusinessException(ErrorCode.PHONE_NOT_VERIFIED);
        }

        if (userRepository.existsByPhone(phone)) {
            throw new BusinessException(ErrorCode.ALREADY_REGISTERED);
        }

        User user = User.builder()
                .phone(phone)
                .password(passwordEncoder.encode(reqDto.getPassword()))
                .role(reqDto.getRole())
                .build();
        userRepository.save(user);
        verifiedPhoneRedisRepository.delete(phone);

        String accessToken = jwtProvider.generateAccessToken(phone);
        String refreshToken = jwtProvider.generateRefreshToken(phone);
        refreshTokenRedisRepository.save(refreshToken, phone);

        return new RegisterRespDto(accessToken, refreshToken, user);
    }

    public LoginRespDto login(LoginReqDto reqDto) {
        String phone = reqDto.getPhone();

        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "가입되지 않은 전화번호입니다"));

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

    private String generateCode() {
        return String.format("%06d", new Random().nextInt(1_000_000));
    }
}
