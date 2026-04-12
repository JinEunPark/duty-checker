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

    private AddConnectionRespDto(Long id, String phone, String name, ConnectionStatus status) {
        this.id = id;
        this.phone = phone;
        this.name = name;
        this.status = status;
    }

    public static AddConnectionRespDto forSubjectRequester(Connection connection) {
        String phone = connection.getGuardian().getPhone();
        String name = connection.getSubjectGivenName() != null
                ? connection.getSubjectGivenName()
                : phone;
        return new AddConnectionRespDto(connection.getId(), phone, name, connection.getStatus());
    }

    public static AddConnectionRespDto forGuardianRequester(Connection connection) {
        String phone = connection.getSubject().getPhone();
        String name = connection.getGuardianGivenName() != null
                ? connection.getGuardianGivenName()
                : phone;
        return new AddConnectionRespDto(connection.getId(), phone, name, connection.getStatus());
    }
}
