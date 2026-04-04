package com.guegue.duty_checker.auth.controller;

import com.guegue.duty_checker.auth.dto.*;
import com.guegue.duty_checker.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/send-code")
    public ResponseEntity<SendCodeRespDto> sendCode(@Valid @RequestBody SendCodeReqDto reqDto) {
        return ResponseEntity.ok(authService.sendCode(reqDto));
    }

    @PostMapping("/verify-code")
    public ResponseEntity<Void> verifyCode(@Valid @RequestBody VerifyCodeReqDto reqDto) {
        authService.verifyCode(reqDto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterRespDto> register(@Valid @RequestBody RegisterReqDto reqDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(reqDto));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginRespDto> login(@Valid @RequestBody LoginReqDto reqDto) {
        return ResponseEntity.ok(authService.login(reqDto));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal String phone) {
        authService.logout(phone);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenRespDto> refresh(@Valid @RequestBody RefreshTokenReqDto reqDto) {
        return ResponseEntity.ok(authService.refresh(reqDto));
    }
}
