package com.guegue.duty_checker.common.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Health", description = "서버 상태 확인 API")
@RestController
public class HealthController {

    @Operation(summary = "헬스 체크", description = "서버가 정상적으로 동작 중인지 확인합니다.")
    @ApiResponse(responseCode = "200", description = "서버 정상")
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }
}
