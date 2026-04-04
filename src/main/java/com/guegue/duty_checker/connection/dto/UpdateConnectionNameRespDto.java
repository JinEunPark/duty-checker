package com.guegue.duty_checker.connection.dto;

import com.guegue.duty_checker.connection.domain.Connection;
import com.guegue.duty_checker.connection.domain.ConnectionStatus;
import com.guegue.duty_checker.user.domain.Role;
import lombok.Getter;

@Getter
public class UpdateConnectionNameRespDto {

    private final Long id;
    private final String phone;
    private final String name;
    private final ConnectionStatus status;

    public static UpdateConnectionNameRespDto forSubject(Connection connection) {
        String phone = connection.getGuardianPhone();
        String name = connection.getSubjectGivenName() != null
                ? connection.getSubjectGivenName()
                : phone;
        return new UpdateConnectionNameRespDto(connection.getId(), phone, name, connection.getStatus());
    }

    public static UpdateConnectionNameRespDto forGuardian(Connection connection) {
        String phone = connection.getSubject().getPhone();
        String name = connection.getGuardianGivenName() != null
                ? connection.getGuardianGivenName()
                : phone;
        return new UpdateConnectionNameRespDto(connection.getId(), phone, name, connection.getStatus());
    }

    private UpdateConnectionNameRespDto(Long id, String phone, String name, ConnectionStatus status) {
        this.id = id;
        this.phone = phone;
        this.name = name;
        this.status = status;
    }
}
