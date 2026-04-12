package com.guegue.duty_checker.connection.controller;

import com.guegue.duty_checker.connection.dto.AddConnectionReqDto;
import com.guegue.duty_checker.connection.dto.AddConnectionRespDto;
import com.guegue.duty_checker.connection.dto.GetConnectionsRespDto;
import com.guegue.duty_checker.connection.dto.UpdateConnectionNameReqDto;
import com.guegue.duty_checker.connection.dto.UpdateConnectionNameRespDto;
import com.guegue.duty_checker.connection.dto.UpdateConnectionStatusReqDto;
import com.guegue.duty_checker.connection.dto.UpdateConnectionStatusRespDto;
import com.guegue.duty_checker.connection.service.ConnectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Connection", description = "연결(안부 대상) 관리 API")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/v1/connections")
@RequiredArgsConstructor
public class ConnectionController {

    private final ConnectionService connectionService;

    @Operation(summary = "연결 신청", description = "전화번호로 안부를 주고받을 상대방에게 연결을 신청합니다. 당사자/보호자 모두 신청 가능하며, 상대방이 수락해야 CONNECTED가 됩니다. 동일 역할 간 연결은 불가합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "연결 신청 성공 (PENDING)"),
            @ApiResponse(responseCode = "400", description = "동일 역할 간 연결 시도"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "404", description = "대상 사용자가 미가입"),
            @ApiResponse(responseCode = "409", description = "이미 연결 요청이 존재함")
    })
    @PostMapping
    public ResponseEntity<AddConnectionRespDto> addConnection(
            @AuthenticationPrincipal String phone,
            @Valid @RequestBody AddConnectionReqDto reqDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(connectionService.addConnection(phone, reqDto));
    }

    @Operation(summary = "연결 수락/거절", description = "연결 신청을 받은 상대방이 수락하거나 거절합니다. 신청자 본인은 호출할 수 없으며, PENDING 상태인 경우에만 처리 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "처리 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 status 값 (CONNECTED 또는 REJECTED만 허용)"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "403", description = "신청자 본인이 호출"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 연결 ID"),
            @ApiResponse(responseCode = "409", description = "이미 처리된 연결 요청")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<UpdateConnectionStatusRespDto> updateConnectionStatus(
            @Parameter(description = "연결 ID") @PathVariable Long id,
            @AuthenticationPrincipal String phone,
            @Valid @RequestBody UpdateConnectionStatusReqDto reqDto) {
        return ResponseEntity.ok(connectionService.updateConnectionStatus(id, phone, reqDto));
    }

    @Operation(summary = "연결 목록 조회", description = "로그인한 사용자의 모든 연결 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청")
    })
    @GetMapping
    public ResponseEntity<GetConnectionsRespDto> getConnections(@AuthenticationPrincipal String phone) {
        return ResponseEntity.ok(connectionService.getConnections(phone));
    }

    @Operation(summary = "연결 이름 수정", description = "특정 연결 대상의 표시 이름을 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "이름 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "403", description = "본인 소유의 연결이 아님"),
            @ApiResponse(responseCode = "404", description = "연결을 찾을 수 없음")
    })
    @PatchMapping("/{id}/name")
    public ResponseEntity<UpdateConnectionNameRespDto> updateConnectionName(
            @Parameter(description = "연결 ID") @PathVariable Long id,
            @AuthenticationPrincipal String phone,
            @Valid @RequestBody UpdateConnectionNameReqDto reqDto) {
        return ResponseEntity.ok(connectionService.updateConnectionName(id, phone, reqDto));
    }

    @Operation(summary = "관계 삭제", description = "연결된 보호자 또는 당사자와의 관계를 삭제합니다. 본인이 subject 또는 guardian으로 참여한 연결만 삭제 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "403", description = "본인과 관련된 연결이 아님"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 연결 ID")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConnection(
            @Parameter(description = "삭제할 연결 관계 ID") @PathVariable Long id,
            @AuthenticationPrincipal String phone) {
        connectionService.deleteConnection(id, phone);
        return ResponseEntity.noContent().build();
    }
}
