package com.guegue.duty_checker.connection.dto;

import com.guegue.duty_checker.user.domain.Role;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class GetConnectionsRespDto {

    private final Role role;
    private final List<ConnectionItemDto> connections;
}
