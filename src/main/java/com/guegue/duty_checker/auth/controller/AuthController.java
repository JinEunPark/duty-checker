package com.guegue.duty_checker.auth.controller;

import com.guegue.duty_checker.auth.dto.*;
import com.guegue.duty_checker.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "인증/인가 API")
@Validated
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "전화번호 가입 여부 확인", description = "전화번호로 이미 가입된 사용자인지 확인합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 전화번호 형식")
    })
    @GetMapping("/check-phone")
    public ResponseEntity<CheckPhoneRespDto> checkPhone(
            @Parameter(description = "확인할 전화번호 (숫자 10~11자리, 예: 01012345678)")
            @RequestParam @Pattern(regexp = "^01[0-9]{8,9}$", message = "전화번호 형식이 올바르지 않습니다") String phone
    ) {
        return ResponseEntity.ok(authService.checkPhone(phone));
    }

    @Operation(summary = "인증 코드 발송", description = "입력한 전화번호로 SMS 인증 코드를 발송합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "인증 코드 발송 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효하지 않은 전화번호 형식 등)"),
            @ApiResponse(responseCode = "429", description = "재발송 대기 시간 미경과")
    })
    @PostMapping("/send-code")
    public ResponseEntity<SendCodeRespDto> sendCode(@Valid @RequestBody SendCodeReqDto reqDto) {
        return ResponseEntity.ok(authService.sendCode(reqDto));
    }

    @Operation(summary = "인증 코드 검증", description = "발송된 SMS 인증 코드의 유효성을 검증합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "인증 코드 검증 성공"),
            @ApiResponse(responseCode = "400", description = "인증 코드 불일치 또는 만료"),
            @ApiResponse(responseCode = "429", description = "인증 시도 횟수 초과")
    })
    @PostMapping("/verify-code")
    public ResponseEntity<Void> verifyCode(@Valid @RequestBody VerifyCodeReqDto reqDto) {
        authService.verifyCode(reqDto);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "회원가입", description = "전화번호로 신규 회원을 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "409", description = "이미 가입된 전화번호")
    })
    @PostMapping("/register")
    public ResponseEntity<RegisterRespDto> register(@Valid @RequestBody RegisterReqDto reqDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(reqDto));
    }

    @Operation(summary = "로그인", description = "전화번호와 비밀번호로 로그인하여 JWT 토큰을 발급받습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공 (Access Token, Refresh Token 발급)"),
            @ApiResponse(responseCode = "401", description = "전화번호 또는 비밀번호 불일치")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginRespDto> login(@Valid @RequestBody LoginReqDto reqDto) {
        return ResponseEntity.ok(authService.login(reqDto));
    }

    @Operation(summary = "토큰 갱신", description = "Refresh Token을 사용하여 새로운 Access Token을 발급합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않거나 만료된 Refresh Token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenRespDto> refresh(@Valid @RequestBody RefreshTokenReqDto reqDto) {
        return ResponseEntity.ok(authService.refresh(reqDto));
    }
}
