package com.guegue.duty_checker.checkin.controller;

import com.guegue.duty_checker.checkin.dto.CreateCheckInRespDto;
import com.guegue.duty_checker.checkin.dto.GetLatestCheckInRespDto;
import com.guegue.duty_checker.checkin.service.CheckInService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "CheckIn", description = "안부 확인(체크인) API")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/v1/check-ins")
@RequiredArgsConstructor
public class CheckInController {

    private final CheckInService checkInService;

    @Operation(summary = "체크인 생성", description = "오늘의 안부를 알립니다. 당일 중복 체크인은 허용되지 않습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "체크인 성공"),
            @ApiResponse(responseCode = "400", description = "당일 이미 체크인 완료"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청")
    })
    @PostMapping
    public ResponseEntity<CreateCheckInRespDto> createCheckIn(@AuthenticationPrincipal String phone) {
        return ResponseEntity.status(HttpStatus.CREATED).body(checkInService.createCheckIn(phone));
    }

    @Operation(summary = "최근 체크인 조회", description = "로그인한 사용자의 가장 최근 체크인 기록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청")
    })
    @GetMapping("/latest")
    public ResponseEntity<GetLatestCheckInRespDto> getLatestCheckIn(@AuthenticationPrincipal String phone) {
        return ResponseEntity.ok(checkInService.getLatestCheckIn(phone));
    }
}
