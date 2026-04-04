package com.guegue.duty_checker.connection.dto;

import com.guegue.duty_checker.connection.domain.Connection;
import com.guegue.duty_checker.connection.domain.ConnectionStatus;
import com.guegue.duty_checker.user.domain.Role;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
public class ConnectionItemDto {

    private final Long id;
    private final String phone;
    private final String name;
    private final ConnectionStatus status;
    private final ZonedDateTime latestCheckedAt;
    private final Boolean isTodayChecked;

    public static ConnectionItemDto forSubject(Connection connection) {
        String phone = connection.getGuardianPhone();
        String name = connection.getSubjectGivenName() != null
                ? connection.getSubjectGivenName()
                : phone;
        return new ConnectionItemDto(connection.getId(), phone, name, connection.getStatus(), null, null);
    }

    public static ConnectionItemDto forGuardian(Connection connection) {
        String phone = connection.getSubject().getPhone();
        String name = connection.getGuardianGivenName() != null
                ? connection.getGuardianGivenName()
                : phone;
        return new ConnectionItemDto(connection.getId(), phone, name, connection.getStatus(), null, false);
    }

    private ConnectionItemDto(Long id, String phone, String name,
                               ConnectionStatus status,
                               ZonedDateTime latestCheckedAt,
                               Boolean isTodayChecked) {
        this.id = id;
        this.phone = phone;
        this.name = name;
        this.status = status;
        this.latestCheckedAt = latestCheckedAt;
        this.isTodayChecked = isTodayChecked;
    }
}
