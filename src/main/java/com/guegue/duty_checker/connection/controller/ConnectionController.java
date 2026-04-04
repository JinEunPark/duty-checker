package com.guegue.duty_checker.connection.controller;

import com.guegue.duty_checker.connection.dto.GetConnectionsRespDto;
import com.guegue.duty_checker.connection.dto.UpdateConnectionNameReqDto;
import com.guegue.duty_checker.connection.dto.UpdateConnectionNameRespDto;
import com.guegue.duty_checker.connection.service.ConnectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/connections")
@RequiredArgsConstructor
public class ConnectionController {

    private final ConnectionService connectionService;

    @GetMapping
    public ResponseEntity<GetConnectionsRespDto> getConnections(@AuthenticationPrincipal String phone) {
        return ResponseEntity.ok(connectionService.getConnections(phone));
    }

    @PatchMapping("/{id}/name")
    public ResponseEntity<UpdateConnectionNameRespDto> updateConnectionName(
            @PathVariable Long id,
            @AuthenticationPrincipal String phone,
            @Valid @RequestBody UpdateConnectionNameReqDto reqDto) {
        return ResponseEntity.ok(connectionService.updateConnectionName(id, phone, reqDto));
    }
}
