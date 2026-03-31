package com.guegue.duty_checker.auth.service;

import com.guegue.duty_checker.auth.domain.SmsCode;
import com.guegue.duty_checker.auth.dto.SendCodeReqDto;
import com.guegue.duty_checker.auth.dto.SendCodeRespDto;
import com.guegue.duty_checker.auth.dto.VerifyCodeReqDto;
import com.guegue.duty_checker.auth.dto.VerifyCodeRespDto;
import com.guegue.duty_checker.auth.infrastructure.SmsProvider;
import com.guegue.duty_checker.auth.repository.SmsCodeRepository;
import com.guegue.duty_checker.common.config.JwtProvider;
import com.guegue.duty_checker.common.exception.BusinessException;
import com.guegue.duty_checker.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final SmsCodeRepository smsCodeRepository;
    private final SmsProvider smsProvider;
    private final JwtProvider jwtProvider;

    @Transactional
    public SendCodeRespDto sendCode(SendCodeReqDto reqDto) {
        String phoneNumber = reqDto.getPhoneNumber();
        String code = generateCode();

        SmsCode smsCode = smsCodeRepository.findByPhoneNumber(phoneNumber)
                .map(existing -> {
                    if (existing.isBlocked()) {
                        throw new BusinessException(ErrorCode.AUTH_SEND_LIMIT_EXCEEDED);
                    }
                    existing.resend(code);
                    return existing;
                })
                .orElseGet(() -> SmsCode.create(phoneNumber, code));

        smsCodeRepository.save(smsCode);
        smsProvider.send(phoneNumber, code);

        return new SendCodeRespDto(phoneNumber, smsCode.getExpiresAt());
    }

    @Transactional
    public VerifyCodeRespDto verifyCode(VerifyCodeReqDto reqDto) {
        SmsCode smsCode = smsCodeRepository.findByPhoneNumber(reqDto.getPhoneNumber())
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_CODE_EXPIRED));

        if (smsCode.isExpired()) {
            throw new BusinessException(ErrorCode.AUTH_CODE_EXPIRED);
        }

        if (!smsCode.matches(reqDto.getCode())) {
            throw new BusinessException(ErrorCode.AUTH_CODE_MISMATCH);
        }

        smsCode.verify();

        String accessToken = jwtProvider.generate(reqDto.getPhoneNumber());
        return new VerifyCodeRespDto(accessToken);
    }

    private String generateCode() {
        return String.format("%06d", new Random().nextInt(1_000_000));
    }
}
