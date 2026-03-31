package com.guegue.duty_checker.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Auth
    AUTH_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "AUTH_CODE_EXPIRED", "인증코드가 만료되었습니다"),
    AUTH_CODE_MISMATCH(HttpStatus.BAD_REQUEST, "AUTH_CODE_MISMATCH", "인증코드가 일치하지 않습니다"),
    AUTH_CODE_ATTEMPTS_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "AUTH_CODE_ATTEMPTS_EXCEEDED", "인증 시도 횟수를 초과했습니다. 인증코드를 재발송해주세요"),
    AUTH_SEND_CODE_COOLDOWN(HttpStatus.TOO_MANY_REQUESTS, "SEND_CODE_COOLDOWN", "인증코드 재발송은 %d초 후에 가능합니다"),
    AUTH_VERIFIED_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "AUTH_VERIFIED_TOKEN_INVALID", "유효하지 않은 인증 토큰입니다"),
    AUTH_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_UNAUTHORIZED", "인증이 필요합니다"),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다"),

    // Guardian
    GUARDIAN_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "GUARDIAN_LIMIT_EXCEEDED", "보호자는 최대 5명까지 등록 가능합니다"),
    GUARDIAN_NOT_FOUND(HttpStatus.NOT_FOUND, "GUARDIAN_NOT_FOUND", "보호자를 찾을 수 없습니다"),

    // Common
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "INVALID_PHONE_FORMAT", "전화번호 형식이 올바르지 않습니다"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "서버 오류가 발생했습니다");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
