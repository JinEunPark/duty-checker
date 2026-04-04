package com.guegue.duty_checker.user.controller;

import com.guegue.duty_checker.user.dto.UpdateDeviceTokenReqDto;
import com.guegue.duty_checker.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PatchMapping("/device-token")
    public ResponseEntity<Void> updateDeviceToken(
            @AuthenticationPrincipal String phone,
            @Valid @RequestBody UpdateDeviceTokenReqDto reqDto) {
        userService.updateFcmToken(phone, reqDto.getFcmToken());
        return ResponseEntity.ok().build();
    }
}
