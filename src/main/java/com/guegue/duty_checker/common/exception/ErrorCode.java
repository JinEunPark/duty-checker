package com.guegue.duty_checker.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Auth
    AUTH_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "AUTH_001", "인증코드가 만료되었습니다"),
    AUTH_CODE_MISMATCH(HttpStatus.BAD_REQUEST, "AUTH_002", "인증코드가 일치하지 않습니다"),
    AUTH_SEND_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "AUTH_003", "발송 횟수를 초과했습니다. 30분 후 재시도해주세요"),
    AUTH_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_004", "인증이 필요합니다"),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "사용자를 찾을 수 없습니다"),

    // Guardian
    GUARDIAN_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "GRDNM_001", "보호자는 최대 5명까지 등록 가능합니다"),
    GUARDIAN_NOT_FOUND(HttpStatus.NOT_FOUND, "GRDNM_002", "보호자를 찾을 수 없습니다"),

    // Common
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON_001", "잘못된 요청입니다"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "서버 오류가 발생했습니다");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
