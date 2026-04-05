package com.guegue.duty_checker.connection.service;

import com.guegue.duty_checker.checkin.service.CheckInService;
import com.guegue.duty_checker.checkin.dto.GetLatestCheckInRespDto;
import com.guegue.duty_checker.common.exception.BusinessException;
import com.guegue.duty_checker.common.exception.ErrorCode;
import com.guegue.duty_checker.connection.domain.Connection;
import com.guegue.duty_checker.connection.domain.ConnectionStatus;
import com.guegue.duty_checker.connection.dto.AddConnectionReqDto;
import com.guegue.duty_checker.connection.dto.UpdateConnectionNameReqDto;
import com.guegue.duty_checker.connection.repository.ConnectionRepository;
import com.guegue.duty_checker.user.domain.Role;
import com.guegue.duty_checker.user.domain.User;
import com.guegue.duty_checker.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ConnectionServiceTest {

    @InjectMocks ConnectionService connectionService;

    @Mock ConnectionRepository connectionRepository;
    @Mock UserService userService;
    @Mock CheckInService checkInService;

    private User user(String phone, Role role) {
        return User.builder().phone(phone).password("pw").role(role).build();
    }

    private AddConnectionReqDto addReq(String guardianPhone, String name) {
        AddConnectionReqDto dto = new AddConnectionReqDto();
        ReflectionTestUtils.setField(dto, "guardianPhone", guardianPhone);
        ReflectionTestUtils.setField(dto, "name", name);
        return dto;
    }

    private UpdateConnectionNameReqDto updateNameReq(String name) {
        UpdateConnectionNameReqDto dto = new UpdateConnectionNameReqDto();
        ReflectionTestUtils.setField(dto, "name", name);
        return dto;
    }

    private Connection connection(User subject, User guardian, String guardianPhone, ConnectionStatus status) {
        return Connection.builder()
                .subject(subject)
                .guardian(guardian)
                .guardianPhone(guardianPhone)
                .status(status)
                .build();
    }

    // ─── addConnection ─────────────────────────────────────────────────────

    @Test
    void addConnection_보호자역할_예외발생() {
        User guardian = user("01011111111", Role.GUARDIAN);
        given(userService.getByPhone("01011111111")).willReturn(guardian);

        assertThatThrownBy(() -> connectionService.addConnection("01011111111", addReq("01022222222", "엄마")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.CONNECTION_SUBJECT_ONLY);
    }

    @Test
    void addConnection_중복등록_예외발생() {
        User subject = user("01011111111", Role.SUBJECT);
        given(userService.getByPhone("01011111111")).willReturn(subject);
        given(connectionRepository.existsBySubjectAndGuardianPhone(subject, "01022222222")).willReturn(true);

        assertThatThrownBy(() -> connectionService.addConnection("01011111111", addReq("01022222222", "엄마")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.CONNECTION_ALREADY_EXISTS);
    }

    @Test
    void addConnection_5명초과_예외발생() {
        User subject = user("01011111111", Role.SUBJECT);
        given(userService.getByPhone("01011111111")).willReturn(subject);
        given(connectionRepository.existsBySubjectAndGuardianPhone(subject, "01022222222")).willReturn(false);
        given(connectionRepository.countBySubject(subject)).willReturn(5L);

        assertThatThrownBy(() -> connectionService.addConnection("01011111111", addReq("01022222222", "엄마")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.GUARDIAN_LIMIT_EXCEEDED);
    }

    @Test
    void addConnection_보호자미가입_PENDING생성() {
        User subject = user("01011111111", Role.SUBJECT);
        given(userService.getByPhone("01011111111")).willReturn(subject);
        given(connectionRepository.existsBySubjectAndGuardianPhone(subject, "01022222222")).willReturn(false);
        given(connectionRepository.countBySubject(subject)).willReturn(0L);
        given(userService.findByPhone("01022222222")).willReturn(Optional.empty());

        var resp = connectionService.addConnection("01011111111", addReq("01022222222", "엄마"));

        verify(connectionRepository).save(any(Connection.class));
        assertThat(resp.getStatus()).isEqualTo(ConnectionStatus.PENDING);
    }

    @Test
    void addConnection_보호자가입됨_CONNECTED생성() {
        User subject = user("01011111111", Role.SUBJECT);
        User guardian = user("01022222222", Role.GUARDIAN);
        given(userService.getByPhone("01011111111")).willReturn(subject);
        given(connectionRepository.existsBySubjectAndGuardianPhone(subject, "01022222222")).willReturn(false);
        given(connectionRepository.countBySubject(subject)).willReturn(0L);
        given(userService.findByPhone("01022222222")).willReturn(Optional.of(guardian));

        var resp = connectionService.addConnection("01011111111", addReq("01022222222", "엄마"));

        assertThat(resp.getStatus()).isEqualTo(ConnectionStatus.CONNECTED);
    }

    // ─── activatePendingConnections ────────────────────────────────────────

    @Test
    void activatePendingConnections_PENDING연결_CONNECTED전환() {
        User subject = user("01011111111", Role.SUBJECT);
        User guardian = user("01022222222", Role.GUARDIAN);
        Connection pending = connection(subject, null, "01022222222", ConnectionStatus.PENDING);
        given(connectionRepository.findByGuardianPhoneAndStatus("01022222222", ConnectionStatus.PENDING))
                .willReturn(List.of(pending));

        connectionService.activatePendingConnections("01022222222", guardian);

        assertThat(pending.getGuardian()).isEqualTo(guardian);
        assertThat(pending.getStatus()).isEqualTo(ConnectionStatus.CONNECTED);
    }

    // ─── getConnections ────────────────────────────────────────────────────

    @Test
    void getConnections_당사자_본인연결목록반환() {
        User subject = user("01011111111", Role.SUBJECT);
        User guardian = user("01022222222", Role.GUARDIAN);
        Connection conn = connection(subject, guardian, "01022222222", ConnectionStatus.CONNECTED);
        given(userService.getByPhone("01011111111")).willReturn(subject);
        given(connectionRepository.findBySubject(subject)).willReturn(List.of(conn));

        var resp = connectionService.getConnections("01011111111");

        assertThat(resp.getRole()).isEqualTo(Role.SUBJECT);
        assertThat(resp.getConnections()).hasSize(1);
    }

    @Test
    void getConnections_보호자_담당연결목록반환() {
        User subject = user("01011111111", Role.SUBJECT);
        User guardian = user("01022222222", Role.GUARDIAN);
        Connection conn = connection(subject, guardian, "01022222222", ConnectionStatus.CONNECTED);
        given(userService.getByPhone("01022222222")).willReturn(guardian);
        given(connectionRepository.findByGuardian(guardian)).willReturn(List.of(conn));
        given(checkInService.getLatestCheckInBySubject(subject))
                .willReturn(new GetLatestCheckInRespDto(null, false));

        var resp = connectionService.getConnections("01022222222");

        assertThat(resp.getRole()).isEqualTo(Role.GUARDIAN);
        assertThat(resp.getConnections()).hasSize(1);
    }

    // ─── updateConnectionName ──────────────────────────────────────────────

    @Test
    void updateConnectionName_당사자_본인연결_이름수정() {
        User subject = user("01011111111", Role.SUBJECT);
        Connection conn = connection(subject, null, "01022222222", ConnectionStatus.PENDING);
        ReflectionTestUtils.setField(conn, "id", 1L);
        ReflectionTestUtils.setField(subject, "id", 1L);
        given(userService.getByPhone("01011111111")).willReturn(subject);
        given(connectionRepository.findById(1L)).willReturn(Optional.of(conn));

        connectionService.updateConnectionName(1L, "01011111111", updateNameReq("새이름"));

        assertThat(conn.getSubjectGivenName()).isEqualTo("새이름");
    }

    @Test
    void updateConnectionName_당사자_다른연결_예외발생() {
        User subject = user("01011111111", Role.SUBJECT);
        User other = user("01099999999", Role.SUBJECT);
        ReflectionTestUtils.setField(subject, "id", 1L);
        ReflectionTestUtils.setField(other, "id", 2L);
        Connection conn = connection(other, null, "01022222222", ConnectionStatus.PENDING);
        given(userService.getByPhone("01011111111")).willReturn(subject);
        given(connectionRepository.findById(1L)).willReturn(Optional.of(conn));

        assertThatThrownBy(() -> connectionService.updateConnectionName(1L, "01011111111", updateNameReq("새이름")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.CONNECTION_FORBIDDEN);
    }

    @Test
    void updateConnectionName_보호자_본인연결_이름수정() {
        User subject = user("01011111111", Role.SUBJECT);
        User guardian = user("01022222222", Role.GUARDIAN);
        ReflectionTestUtils.setField(guardian, "id", 2L);
        Connection conn = connection(subject, guardian, "01022222222", ConnectionStatus.CONNECTED);
        given(userService.getByPhone("01022222222")).willReturn(guardian);
        given(connectionRepository.findById(1L)).willReturn(Optional.of(conn));

        connectionService.updateConnectionName(1L, "01022222222", updateNameReq("새이름"));

        assertThat(conn.getGuardianGivenName()).isEqualTo("새이름");
    }

    @Test
    void updateConnectionName_보호자_다른연결_예외발생() {
        User guardian = user("01022222222", Role.GUARDIAN);
        User otherGuardian = user("01033333333", Role.GUARDIAN);
        ReflectionTestUtils.setField(guardian, "id", 2L);
        ReflectionTestUtils.setField(otherGuardian, "id", 3L);
        Connection conn = connection(user("01011111111", Role.SUBJECT), otherGuardian, "01033333333", ConnectionStatus.CONNECTED);
        given(userService.getByPhone("01022222222")).willReturn(guardian);
        given(connectionRepository.findById(1L)).willReturn(Optional.of(conn));

        assertThatThrownBy(() -> connectionService.updateConnectionName(1L, "01022222222", updateNameReq("새이름")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.CONNECTION_FORBIDDEN);
    }
}
