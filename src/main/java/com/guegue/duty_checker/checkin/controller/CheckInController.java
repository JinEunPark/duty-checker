package com.guegue.duty_checker.checkin.controller;

import com.guegue.duty_checker.checkin.dto.CreateCheckInRespDto;
import com.guegue.duty_checker.checkin.dto.GetLatestCheckInRespDto;
import com.guegue.duty_checker.checkin.service.CheckInService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/check-ins")
@RequiredArgsConstructor
public class CheckInController {

    private final CheckInService checkInService;

    @PostMapping
    public ResponseEntity<CreateCheckInRespDto> createCheckIn(@AuthenticationPrincipal String phone) {
        return ResponseEntity.status(HttpStatus.CREATED).body(checkInService.createCheckIn(phone));
    }

    @GetMapping("/latest")
    public ResponseEntity<GetLatestCheckInRespDto> getLatestCheckIn(@AuthenticationPrincipal String phone) {
        return ResponseEntity.ok(checkInService.getLatestCheckIn(phone));
    }
}
