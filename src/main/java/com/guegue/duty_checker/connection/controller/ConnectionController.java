package com.guegue.duty_checker.connection.controller;

import com.guegue.duty_checker.connection.dto.AddConnectionReqDto;
import com.guegue.duty_checker.connection.dto.AddConnectionRespDto;
import com.guegue.duty_checker.connection.dto.GetConnectionsRespDto;
import com.guegue.duty_checker.connection.dto.UpdateConnectionNameReqDto;
import com.guegue.duty_checker.connection.dto.UpdateConnectionNameRespDto;
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

    @Operation(summary = "연결 추가", description = "전화번호로 안부를 주고받을 상대방을 연결합니다. 상대방이 앱에 가입되어 있어야 합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "연결 추가 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 이미 연결된 대상"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "404", description = "상대방을 찾을 수 없음")
    })
    @PostMapping
    public ResponseEntity<AddConnectionRespDto> addConnection(
            @AuthenticationPrincipal String phone,
            @Valid @RequestBody AddConnectionReqDto reqDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(connectionService.addConnection(phone, reqDto));
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
