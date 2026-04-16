package com.guegue.duty_checker.connection.dto;

import com.guegue.duty_checker.connection.domain.Connection;
import com.guegue.duty_checker.connection.domain.ConnectionStatus;
import com.guegue.duty_checker.user.domain.Role;
import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description = "연결을 신청한 사용자의 역할 (SUBJECT 또는 GUARDIAN)")
    private final Role requesterRole;

    public static ConnectionItemDto forSubject(Connection connection) {
        String phone = connection.getGuardianPhone();
        String name = connection.getSubjectGivenName() != null
                ? connection.getSubjectGivenName()
                : phone;
        return new ConnectionItemDto(connection.getId(), phone, name, connection.getStatus(), null, null, connection.getRequester().getRole());
    }

    public static ConnectionItemDto forGuardian(Connection connection, ZonedDateTime latestCheckedAt, boolean isTodayChecked) {
        String phone = connection.getSubject().getPhone();
        String name = connection.getGuardianGivenName() != null
                ? connection.getGuardianGivenName()
                : phone;
        return new ConnectionItemDto(connection.getId(), phone, name, connection.getStatus(), latestCheckedAt, isTodayChecked, connection.getRequester().getRole());
    }

    private ConnectionItemDto(Long id, String phone, String name,
                               ConnectionStatus status,
                               ZonedDateTime latestCheckedAt,
                               Boolean isTodayChecked,
                               Role requesterRole) {
        this.id = id;
        this.phone = phone;
        this.name = name;
        this.status = status;
        this.latestCheckedAt = latestCheckedAt;
        this.isTodayChecked = isTodayChecked;
        this.requesterRole = requesterRole;
    }
}
