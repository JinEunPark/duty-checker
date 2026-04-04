package com.guegue.duty_checker.connection.dto;

import com.guegue.duty_checker.connection.domain.Connection;
import com.guegue.duty_checker.connection.domain.ConnectionStatus;
import lombok.Getter;

@Getter
public class AddConnectionRespDto {

    private final Long id;
    private final String phone;
    private final String name;
    private final ConnectionStatus status;

    public AddConnectionRespDto(Connection connection) {
        this.id = connection.getId();
        this.phone = connection.getGuardianPhone();
        this.name = connection.getSubjectGivenName() != null
                ? connection.getSubjectGivenName()
                : connection.getGuardianPhone();
        this.status = connection.getStatus();
    }
}
