package com.guegue.duty_checker.user.controller;

import com.guegue.duty_checker.auth.service.AuthService;
import com.guegue.duty_checker.user.dto.UpdateDeviceTokenReqDto;
import com.guegue.duty_checker.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User", description = "사용자 정보 API")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    @Operation(summary = "FCM 디바이스 토큰 업데이트", description = "푸시 알림 수신을 위한 FCM 디바이스 토큰을 등록하거나 갱신합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 업데이트 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청")
    })
    @PatchMapping("/device-token")
    public ResponseEntity<Void> updateDeviceToken(
            @AuthenticationPrincipal String phone,
            @Valid @RequestBody UpdateDeviceTokenReqDto reqDto) {
        userService.updateFcmToken(phone, reqDto.getFcmToken());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "로그아웃", description = "현재 사용자의 Refresh Token을 무효화하여 로그아웃 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal String phone) {
        authService.logout(phone);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "회원탈퇴", description = "현재 사용자의 계정을 탈퇴 처리합니다. 연결된 Connection과 CheckIn 데이터가 삭제되며 복구할 수 없습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "회원탈퇴 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @DeleteMapping("/me")
    public ResponseEntity<Void> withdraw(@AuthenticationPrincipal String phone) {
        authService.withdraw(phone);
        return ResponseEntity.noContent().build();
    }
}
