package com.guegue.duty_checker.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Auth
    AUTH_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다"),
    ALREADY_REGISTERED(HttpStatus.CONFLICT, "ALREADY_REGISTERED", "이미 가입된 전화번호입니다"),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "INVALID_PASSWORD", "비밀번호가 올바르지 않습니다"),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "refreshToken이 만료되었거나 유효하지 않습니다. 다시 로그인해주세요."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다"),

    // Guardian
    GUARDIAN_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "GUARDIAN_LIMIT_EXCEEDED", "보호자는 최대 5명까지 등록 가능합니다"),
    GUARDIAN_NOT_FOUND(HttpStatus.NOT_FOUND, "GUARDIAN_NOT_FOUND", "보호자를 찾을 수 없습니다"),

    // Connection
    CONNECTION_NOT_FOUND(HttpStatus.NOT_FOUND, "CONNECTION_NOT_FOUND", "연결 정보를 찾을 수 없습니다"),
    CONNECTION_FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "수정 권한이 없습니다"),
    CONNECTION_SUBJECT_ONLY(HttpStatus.FORBIDDEN, "FORBIDDEN", "당사자만 보호자를 추가할 수 있습니다"),
    CONNECTION_ALREADY_EXISTS(HttpStatus.CONFLICT, "CONNECTION_ALREADY_EXISTS", "이미 등록된 보호자입니다"),

    // CheckIn
    ALREADY_CHECKED_IN(HttpStatus.CONFLICT, "ALREADY_CHECKED_IN", "오늘은 이미 안부 확인을 했습니다"),
    CHECK_IN_FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "당사자만 안부 확인이 가능합니다"),

    // Common
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "INVALID_PHONE_FORMAT", "전화번호 형식이 올바르지 않습니다"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "서버 오류가 발생했습니다");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
