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
import com.guegue.duty_checker.connection.repository.ConnectionRepository;
import com.guegue.duty_checker.user.domain.Role;
import com.guegue.duty_checker.user.domain.User;
import com.guegue.duty_checker.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConnectionService {

    private final ConnectionRepository connectionRepository;
    private final UserService userService;
    private final CheckInService checkInService;

    @Transactional
    public AddConnectionRespDto addConnection(String subjectPhone, AddConnectionReqDto reqDto) {
        User subject = userService.getByPhone(subjectPhone);

        if (subject.getRole() != Role.SUBJECT) {
            throw new BusinessException(ErrorCode.CONNECTION_SUBJECT_ONLY);
        }

        if (connectionRepository.existsBySubjectAndGuardianPhone(subject, reqDto.getGuardianPhone())) {
            throw new BusinessException(ErrorCode.CONNECTION_ALREADY_EXISTS);
        }

        if (connectionRepository.countBySubject(subject) >= 5) {
            throw new BusinessException(ErrorCode.GUARDIAN_LIMIT_EXCEEDED);
        }

        User guardian = userService.findByPhone(reqDto.getGuardianPhone()).orElse(null);
        ConnectionStatus status = guardian != null ? ConnectionStatus.CONNECTED : ConnectionStatus.PENDING;

        Connection connection = Connection.builder()
                .subject(subject)
                .guardian(guardian)
                .guardianPhone(reqDto.getGuardianPhone())
                .subjectGivenName(reqDto.getName())
                .status(status)
                .build();
        connectionRepository.save(connection);

        return new AddConnectionRespDto(connection);
    }

    @Transactional
    public void activatePendingConnections(String guardianPhone, User guardian) {
        List<Connection> pending = connectionRepository.findByGuardianPhoneAndStatus(guardianPhone, ConnectionStatus.PENDING);
        pending.forEach(c -> c.connectGuardian(guardian));
    }

    @Transactional(readOnly = true)
    public GetConnectionsRespDto getConnections(String phone) {
        User user = userService.getByPhone(phone);

        if (user.getRole() == Role.SUBJECT) {
            List<ConnectionItemDto> items = connectionRepository.findBySubject(user).stream()
                    .map(ConnectionItemDto::forSubject)
                    .toList();
            return new GetConnectionsRespDto(Role.GUARDIAN, items);
        } else {
            List<ConnectionItemDto> items = connectionRepository.findByGuardian(user).stream()
                    .map(connection -> {
                        GetLatestCheckInRespDto checkIn = checkInService.getLatestCheckInBySubject(connection.getSubject());
                        return ConnectionItemDto.forGuardian(connection, checkIn.getLatestCheckedAt(), checkIn.isTodayChecked());
                    })
                    .toList();
            return new GetConnectionsRespDto(Role.SUBJECT, items);
        }
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
}
