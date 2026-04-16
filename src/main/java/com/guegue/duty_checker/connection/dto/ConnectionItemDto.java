package com.guegue.duty_checker.connection.dto;

import com.guegue.duty_checker.connection.domain.Connection;
import com.guegue.duty_checker.connection.domain.ConnectionStatus;
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

    @Schema(description = "현재 사용자가 이 연결을 신청한 쪽이면 true, 신청을 받은 쪽이면 false")
    private final Boolean sentByMe;

    public static ConnectionItemDto forSubject(Connection connection, Long currentUserId) {
        String phone = connection.getGuardianPhone();
        String name = connection.getSubjectGivenName() != null
                ? connection.getSubjectGivenName()
                : phone;
        boolean sentByMe = connection.getRequester().getId().equals(currentUserId);
        return new ConnectionItemDto(connection.getId(), phone, name, connection.getStatus(), null, null, sentByMe);
    }

    public static ConnectionItemDto forGuardian(Connection connection, ZonedDateTime latestCheckedAt, boolean isTodayChecked, Long currentUserId) {
        String phone = connection.getSubject().getPhone();
        String name = connection.getGuardianGivenName() != null
                ? connection.getGuardianGivenName()
                : phone;
        boolean sentByMe = connection.getRequester().getId().equals(currentUserId);
        return new ConnectionItemDto(connection.getId(), phone, name, connection.getStatus(), latestCheckedAt, isTodayChecked, sentByMe);
    }

    private ConnectionItemDto(Long id, String phone, String name,
                               ConnectionStatus status,
                               ZonedDateTime latestCheckedAt,
                               Boolean isTodayChecked,
                               Boolean sentByMe) {
        this.id = id;
        this.phone = phone;
        this.name = name;
        this.status = status;
        this.latestCheckedAt = latestCheckedAt;
        this.isTodayChecked = isTodayChecked;
        this.sentByMe = sentByMe;
    }
}
