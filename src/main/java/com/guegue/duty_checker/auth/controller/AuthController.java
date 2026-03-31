package com.guegue.duty_checker.auth.controller;

import com.guegue.duty_checker.auth.dto.SendCodeReqDto;
import com.guegue.duty_checker.auth.dto.SendCodeRespDto;
import com.guegue.duty_checker.auth.dto.VerifyCodeReqDto;
import com.guegue.duty_checker.auth.dto.VerifyCodeRespDto;
import com.guegue.duty_checker.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<VerifyCodeRespDto> verifyCode(@Valid @RequestBody VerifyCodeReqDto reqDto) {
        return ResponseEntity.ok(authService.verifyCode(reqDto));
    }

    @PostMapping("/resend-code")
    public ResponseEntity<SendCodeRespDto> resendCode(@Valid @RequestBody SendCodeReqDto reqDto) {
        return ResponseEntity.ok(authService.sendCode(reqDto));
    }
}
