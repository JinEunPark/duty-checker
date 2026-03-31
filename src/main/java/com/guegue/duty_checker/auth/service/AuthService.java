package com.guegue.duty_checker.auth.service;

import com.guegue.duty_checker.auth.dto.*;
import com.guegue.duty_checker.auth.infrastructure.SmsCodeRedisRepository;
import com.guegue.duty_checker.auth.infrastructure.SmsProvider;
import com.guegue.duty_checker.auth.infrastructure.VerifiedTokenRedisRepository;
import com.guegue.duty_checker.common.config.JwtProvider;
import com.guegue.duty_checker.common.exception.BusinessException;
import com.guegue.duty_checker.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final SmsCodeRedisRepository smsCodeRedisRepository;
    private final VerifiedTokenRedisRepository verifiedTokenRedisRepository;
    private final SmsProvider smsProvider;
    private final JwtProvider jwtProvider;

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

    public VerifyCodeRespDto verifyCode(VerifyCodeReqDto reqDto) {
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

        String verifiedToken = jwtProvider.generateVerifiedToken();
        verifiedTokenRedisRepository.save(verifiedToken, phone);

        return new VerifyCodeRespDto(verifiedToken);
    }

    private String generateCode() {
        return String.format("%06d", new Random().nextInt(1_000_000));
    }
}
