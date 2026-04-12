package com.guegue.duty_checker.connection.dto;

import com.guegue.duty_checker.connection.domain.ConnectionStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UpdateConnectionStatusRespDto {

    private final Long id;
    private final ConnectionStatus status;
}
