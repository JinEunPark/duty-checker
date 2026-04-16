package com.guegue.duty_checker.connection.service;

import com.guegue.duty_checker.checkin.dto.GetLatestCheckInRespDto;
import com.guegue.duty_checker.checkin.service.CheckInService;
import com.guegue.duty_checker.common.exception.BusinessException;
import com.guegue.duty_checker.common.exception.ErrorCode;
import com.guegue.duty_checker.connection.domain.Connection;
import com.guegue.duty_checker.connection.domain.ConnectionStatus;
import com.guegue.duty_checker.connection.dto.AddConnectionReqDto;
import com.guegue.duty_checker.connection.dto.AddConnectionRespDto;
import com.guegue.duty_checker.connection.dto.ConnectionItemDto;
import com.guegue.duty_checker.connection.dto.GetConnectionsRespDto;
import com.guegue.duty_checker.connection.dto.UpdateConnectionNameReqDto;
import com.guegue.duty_checker.connection.dto.UpdateConnectionNameRespDto;
import com.guegue.duty_checker.connection.dto.UpdateConnectionStatusReqDto;
import com.guegue.duty_checker.connection.dto.UpdateConnectionStatusRespDto;
import com.guegue.duty_checker.connection.repository.ConnectionRepository;
import com.guegue.duty_checker.notification.service.NotificationService;
import com.guegue.duty_checker.user.domain.Role;
import com.guegue.duty_checker.user.domain.User;
import com.guegue.duty_checker.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ConnectionService {

    private final ConnectionRepository connectionRepository;
    private final UserService userService;
    private final CheckInService checkInService;
    private final NotificationService notificationService;

    @Transactional
    public AddConnectionRespDto addConnection(String requesterPhone, AddConnectionReqDto reqDto) {
        User requester = userService.getByPhone(requesterPhone);
        User target = userService.findByPhone(reqDto.getTargetPhone())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        validateRoles(requester, target);

        User subject = requester.getRole() == Role.SUBJECT ? requester : target;
        User guardian = requester.getRole() == Role.GUARDIAN ? requester : target;

        if (connectionRepository.existsBySubjectAndGuardianAndStatusInAndDeletedAtIsNull(
                subject, guardian, List.of(ConnectionStatus.PENDING, ConnectionStatus.CONNECTED))) {
            throw new BusinessException(ErrorCode.CONNECTION_ALREADY_EXISTS);
        }

        Connection connection = Connection.builder()
                .subject(subject)
                .guardian(guardian)
                .guardianPhone(guardian.getPhone())
                .requester(requester)
                .subjectGivenName(requester.getRole() == Role.SUBJECT ? reqDto.getName() : null)
                .guardianGivenName(requester.getRole() == Role.GUARDIAN ? reqDto.getName() : null)
                .status(ConnectionStatus.PENDING)
                .build();
        connectionRepository.save(connection);
        notificationService.sendConnectionRequestAlert(target, requester);

        return requester.getRole() == Role.SUBJECT
                ? AddConnectionRespDto.forSubjectRequester(connection)
                : AddConnectionRespDto.forGuardianRequester(connection);
    }

    @Transactional
    public UpdateConnectionStatusRespDto updateConnectionStatus(Long connectionId, String callerPhone,
                                                                 UpdateConnectionStatusReqDto reqDto) {
        Connection connection = findActiveConnectionById(connectionId);
        User caller = userService.getByPhone(callerPhone);

        validateResponder(connection, caller);
        validatePendingStatus(connection);
        ConnectionStatus newStatus = parseStatus(reqDto.getStatus());

        connection.updateStatus(newStatus);
        return new UpdateConnectionStatusRespDto(connection.getId(), connection.getStatus());
    }

    @Transactional(readOnly = true)
    public GetConnectionsRespDto getConnections(String phone) {
        User user = userService.getByPhone(phone);

        if (user.getRole() == Role.SUBJECT) {
            List<ConnectionItemDto> items = connectionRepository.findBySubjectAndDeletedAtIsNull(user).stream()
                    .map(connection -> ConnectionItemDto.forSubject(connection, user.getId()))
                    .toList();
            return new GetConnectionsRespDto(Role.GUARDIAN, items);
        } else {
            List<ConnectionItemDto> items = connectionRepository.findByGuardianAndDeletedAtIsNull(user).stream()
                    .map(connection -> {
                        GetLatestCheckInRespDto checkIn = checkInService.getLatestCheckInBySubject(connection.getSubject());
                        return ConnectionItemDto.forGuardian(connection, checkIn.getLatestCheckedAt(), checkIn.isTodayChecked(), user.getId());
                    })
                    .toList();
            return new GetConnectionsRespDto(Role.SUBJECT, items);
        }
    }

    @Transactional
    public void deleteConnection(Long connectionId, String phone) {
        User user = userService.getByPhone(phone);
        Connection connection = findActiveConnectionById(connectionId);
        validateDeletable(connection, user);
        connection.softDelete();
    }

    @Transactional
    public void deleteAllByUser(User user) {
        connectionRepository.deleteBySubjectOrGuardian(user, user);
    }

    @Transactional
    public UpdateConnectionNameRespDto updateConnectionName(Long connectionId, String phone, UpdateConnectionNameReqDto reqDto) {
        User user = userService.getByPhone(phone);
        Connection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONNECTION_NOT_FOUND));

        if (user.getRole() == Role.SUBJECT) {
            if (!connection.getSubject().getId().equals(user.getId())) {
                throw new BusinessException(ErrorCode.CONNECTION_FORBIDDEN);
            }
            connection.updateSubjectGivenName(reqDto.getName());
            return UpdateConnectionNameRespDto.forSubject(connection);
        } else {
            if (connection.getGuardian() == null || !connection.getGuardian().getId().equals(user.getId())) {
                throw new BusinessException(ErrorCode.CONNECTION_FORBIDDEN);
            }
            connection.updateGuardianGivenName(reqDto.getName());
            return UpdateConnectionNameRespDto.forGuardian(connection);
        }
    }

    private void validateRoles(User requester, User target) {
        if (requester.getRole() == target.getRole()) {
            throw new BusinessException(ErrorCode.INVALID_CONNECTION_ROLES);
        }
    }

    private void validateResponder(Connection connection, User caller) {
        if (Objects.equals(connection.getRequester().getId(), caller.getId())) {
            throw new BusinessException(ErrorCode.CONNECTION_RESPONDER_ONLY);
        }
    }

    private void validatePendingStatus(Connection connection) {
        if (connection.getStatus() != ConnectionStatus.PENDING) {
            throw new BusinessException(ErrorCode.CONNECTION_ALREADY_PROCESSED);
        }
    }

    private ConnectionStatus parseStatus(String status) {
        if ("CONNECTED".equals(status)) return ConnectionStatus.CONNECTED;
        if ("REJECTED".equals(status)) return ConnectionStatus.REJECTED;
        throw new BusinessException(ErrorCode.INVALID_STATUS);
    }

    private Connection findActiveConnectionById(Long connectionId) {
        Connection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONNECTION_NOT_FOUND));
        if (connection.getDeletedAt() != null) {
            throw new BusinessException(ErrorCode.CONNECTION_NOT_FOUND);
        }
        return connection;
    }

    private void validateDeletable(Connection connection, User user) {
        boolean isSubject = Objects.equals(connection.getSubject().getId(), user.getId());
        boolean isGuardian = connection.getGuardian() != null
                && Objects.equals(connection.getGuardian().getId(), user.getId());
        if (!isSubject && !isGuardian) {
            throw new BusinessException(ErrorCode.CONNECTION_DELETE_FORBIDDEN);
        }
    }
}
