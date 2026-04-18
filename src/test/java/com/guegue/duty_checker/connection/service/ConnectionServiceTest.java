package com.guegue.duty_checker.connection.service;

import com.guegue.duty_checker.checkin.dto.GetLatestCheckInRespDto;
import com.guegue.duty_checker.checkin.service.CheckInService;
import com.guegue.duty_checker.common.exception.BusinessException;
import com.guegue.duty_checker.common.exception.ErrorCode;
import com.guegue.duty_checker.connection.domain.Connection;
import com.guegue.duty_checker.connection.domain.ConnectionStatus;
import com.guegue.duty_checker.connection.dto.AddConnectionReqDto;
import com.guegue.duty_checker.connection.dto.UpdateConnectionNameReqDto;
import com.guegue.duty_checker.connection.dto.UpdateConnectionStatusReqDto;
import com.guegue.duty_checker.connection.repository.ConnectionRepository;
import com.guegue.duty_checker.notification.service.NotificationService;
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
    @Mock NotificationService notificationService;

    private User user(String phone, Role role) {
        User u = User.builder().phone(phone).password("pw").role(role).build();
        return u;
    }

    private User userWithId(String phone, Role role, long id) {
        User u = User.builder().phone(phone).password("pw").role(role).build();
        ReflectionTestUtils.setField(u, "id", id);
        return u;
    }

    private AddConnectionReqDto addReq(String targetPhone, String name) {
        AddConnectionReqDto dto = new AddConnectionReqDto();
        ReflectionTestUtils.setField(dto, "targetPhone", targetPhone);
        ReflectionTestUtils.setField(dto, "name", name);
        return dto;
    }

    private UpdateConnectionStatusReqDto statusReq(String status) {
        UpdateConnectionStatusReqDto dto = new UpdateConnectionStatusReqDto();
        ReflectionTestUtils.setField(dto, "status", status);
        return dto;
    }

    private UpdateConnectionNameReqDto updateNameReq(String name) {
        UpdateConnectionNameReqDto dto = new UpdateConnectionNameReqDto();
        ReflectionTestUtils.setField(dto, "name", name);
        return dto;
    }

    private Connection connection(User subject, User guardian, User requester, ConnectionStatus status) {
        return Connection.builder()
                .subject(subject)
                .guardian(guardian)
                .guardianPhone(guardian != null ? guardian.getPhone() : "01099999999")
                .requester(requester)
                .status(status)
                .build();
    }

    // ─── addConnection ─────────────────────────────────────────────────────────

    @Test
    void addConnection_당사자신청_PENDING생성() {
        User subject = user("01011111111", Role.SUBJECT);
        User guardian = user("01022222222", Role.GUARDIAN);
        given(userService.getByPhone("01011111111")).willReturn(subject);
        given(userService.findByPhone("01022222222")).willReturn(Optional.of(guardian));
        given(connectionRepository.existsBySubjectAndGuardianAndStatusInAndDeletedAtIsNull(
                any(), any(), any())).willReturn(false);

        var resp = connectionService.addConnection("01011111111", addReq("01022222222", "엄마"));

        verify(connectionRepository).save(any(Connection.class));
        assertThat(resp.getStatus()).isEqualTo(ConnectionStatus.PENDING);
        assertThat(resp.getPhone()).isEqualTo("01022222222");
    }

    @Test
    void addConnection_보호자신청_PENDING생성() {
        User subject = user("01011111111", Role.SUBJECT);
        User guardian = user("01022222222", Role.GUARDIAN);
        given(userService.getByPhone("01022222222")).willReturn(guardian);
        given(userService.findByPhone("01011111111")).willReturn(Optional.of(subject));
        given(connectionRepository.existsBySubjectAndGuardianAndStatusInAndDeletedAtIsNull(
                any(), any(), any())).willReturn(false);

        var resp = connectionService.addConnection("01022222222", addReq("01011111111", "홍길동"));

        verify(connectionRepository).save(any(Connection.class));
        assertThat(resp.getStatus()).isEqualTo(ConnectionStatus.PENDING);
        assertThat(resp.getPhone()).isEqualTo("01011111111");
    }

    @Test
    void addConnection_대상미가입_예외발생() {
        User subject = user("01011111111", Role.SUBJECT);
        given(userService.getByPhone("01011111111")).willReturn(subject);
        given(userService.findByPhone("01099999999")).willReturn(Optional.empty());

        assertThatThrownBy(() -> connectionService.addConnection("01011111111", addReq("01099999999", "엄마")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    void addConnection_동일역할신청_예외발생() {
        User subject1 = user("01011111111", Role.SUBJECT);
        User subject2 = user("01022222222", Role.SUBJECT);
        given(userService.getByPhone("01011111111")).willReturn(subject1);
        given(userService.findByPhone("01022222222")).willReturn(Optional.of(subject2));

        assertThatThrownBy(() -> connectionService.addConnection("01011111111", addReq("01022222222", "친구")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_CONNECTION_ROLES);
    }

    @Test
    void addConnection_중복신청_예외발생() {
        User subject = user("01011111111", Role.SUBJECT);
        User guardian = user("01022222222", Role.GUARDIAN);
        given(userService.getByPhone("01011111111")).willReturn(subject);
        given(userService.findByPhone("01022222222")).willReturn(Optional.of(guardian));
        given(connectionRepository.existsBySubjectAndGuardianAndStatusInAndDeletedAtIsNull(
                any(), any(), any())).willReturn(true);

        assertThatThrownBy(() -> connectionService.addConnection("01011111111", addReq("01022222222", "엄마")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.CONNECTION_ALREADY_EXISTS);
    }

    // ─── updateConnectionStatus ────────────────────────────────────────────────

    @Test
    void updateConnectionStatus_수락_CONNECTED전환() {
        User subject = userWithId("01011111111", Role.SUBJECT, 1L);
        User guardian = userWithId("01022222222", Role.GUARDIAN, 2L);
        Connection conn = connection(subject, guardian, subject, ConnectionStatus.PENDING);
        ReflectionTestUtils.setField(conn, "id", 10L);
        given(connectionRepository.findById(10L)).willReturn(Optional.of(conn));
        given(userService.getByPhone("01022222222")).willReturn(guardian);

        var resp = connectionService.updateConnectionStatus(10L, "01022222222", statusReq("CONNECTED"));

        assertThat(resp.getStatus()).isEqualTo(ConnectionStatus.CONNECTED);
        assertThat(resp.getId()).isEqualTo(10L);
    }

    @Test
    void updateConnectionStatus_거절_REJECTED전환() {
        User subject = userWithId("01011111111", Role.SUBJECT, 1L);
        User guardian = userWithId("01022222222", Role.GUARDIAN, 2L);
        Connection conn = connection(subject, guardian, subject, ConnectionStatus.PENDING);
        ReflectionTestUtils.setField(conn, "id", 10L);
        given(connectionRepository.findById(10L)).willReturn(Optional.of(conn));
        given(userService.getByPhone("01022222222")).willReturn(guardian);

        var resp = connectionService.updateConnectionStatus(10L, "01022222222", statusReq("REJECTED"));

        assertThat(resp.getStatus()).isEqualTo(ConnectionStatus.REJECTED);
    }

    @Test
    void updateConnectionStatus_신청자본인호출_예외발생() {
        User subject = userWithId("01011111111", Role.SUBJECT, 1L);
        User guardian = userWithId("01022222222", Role.GUARDIAN, 2L);
        Connection conn = connection(subject, guardian, subject, ConnectionStatus.PENDING);
        ReflectionTestUtils.setField(conn, "id", 10L);
        given(connectionRepository.findById(10L)).willReturn(Optional.of(conn));
        given(userService.getByPhone("01011111111")).willReturn(subject);

        assertThatThrownBy(() -> connectionService.updateConnectionStatus(10L, "01011111111", statusReq("CONNECTED")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.CONNECTION_RESPONDER_ONLY);
    }

    @Test
    void updateConnectionStatus_PENDING아닌상태_예외발생() {
        User subject = userWithId("01011111111", Role.SUBJECT, 1L);
        User guardian = userWithId("01022222222", Role.GUARDIAN, 2L);
        Connection conn = connection(subject, guardian, subject, ConnectionStatus.CONNECTED);
        ReflectionTestUtils.setField(conn, "id", 10L);
        given(connectionRepository.findById(10L)).willReturn(Optional.of(conn));
        given(userService.getByPhone("01022222222")).willReturn(guardian);

        assertThatThrownBy(() -> connectionService.updateConnectionStatus(10L, "01022222222", statusReq("CONNECTED")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.CONNECTION_ALREADY_PROCESSED);
    }

    @Test
    void updateConnectionStatus_유효하지않은status_예외발생() {
        User subject = userWithId("01011111111", Role.SUBJECT, 1L);
        User guardian = userWithId("01022222222", Role.GUARDIAN, 2L);
        Connection conn = connection(subject, guardian, subject, ConnectionStatus.PENDING);
        ReflectionTestUtils.setField(conn, "id", 10L);
        given(connectionRepository.findById(10L)).willReturn(Optional.of(conn));
        given(userService.getByPhone("01022222222")).willReturn(guardian);

        assertThatThrownBy(() -> connectionService.updateConnectionStatus(10L, "01022222222", statusReq("PENDING")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_STATUS);
    }

    // ─── getConnections ────────────────────────────────────────────────────────

    @Test
    void getConnections_당사자_본인연결목록반환() {
        User subject = userWithId("01011111111", Role.SUBJECT, 1L);
        User guardian = userWithId("01022222222", Role.GUARDIAN, 2L);
        Connection conn = connection(subject, guardian, subject, ConnectionStatus.CONNECTED);
        given(userService.getByPhone("01011111111")).willReturn(subject);
        given(connectionRepository.findBySubjectAndDeletedAtIsNull(subject)).willReturn(List.of(conn));

        var resp = connectionService.getConnections("01011111111");

        assertThat(resp.getRole()).isEqualTo(Role.GUARDIAN);
        assertThat(resp.getConnections()).hasSize(1);
    }

    @Test
    void getConnections_보호자_담당연결목록반환() {
        User subject = userWithId("01011111111", Role.SUBJECT, 1L);
        User guardian = userWithId("01022222222", Role.GUARDIAN, 2L);
        Connection conn = connection(subject, guardian, subject, ConnectionStatus.CONNECTED);
        given(userService.getByPhone("01022222222")).willReturn(guardian);
        given(connectionRepository.findByGuardianAndDeletedAtIsNull(guardian)).willReturn(List.of(conn));
        given(checkInService.getLatestCheckInBySubject(subject))
                .willReturn(new GetLatestCheckInRespDto(null, false));

        var resp = connectionService.getConnections("01022222222");

        assertThat(resp.getRole()).isEqualTo(Role.SUBJECT);
        assertThat(resp.getConnections()).hasSize(1);
    }

    @Test
    void getConnections_requesterNull인레거시연결_NPE없이requesterRoleNull반환() {
        // requester_id가 NULL인 레거시 DB 데이터 시뮬레이션
        User subject = userWithId("01011111111", Role.SUBJECT, 1L);
        User guardian = userWithId("01022222222", Role.GUARDIAN, 2L);
        Connection conn = connection(subject, guardian, null, ConnectionStatus.CONNECTED);
        given(userService.getByPhone("01011111111")).willReturn(subject);
        given(connectionRepository.findBySubjectAndDeletedAtIsNull(subject)).willReturn(List.of(conn));

        var resp = connectionService.getConnections("01011111111");

        assertThat(resp.getConnections()).hasSize(1);
        assertThat(resp.getConnections().get(0).getRequesterRole()).isNull();
    }

    @Test
    void getConnections_보호자_requesterNull인레거시연결_NPE없이requesterRoleNull반환() {
        // 보호자 조회 시 requester_id가 NULL인 레거시 DB 데이터 시뮬레이션
        User subject = userWithId("01011111111", Role.SUBJECT, 1L);
        User guardian = userWithId("01022222222", Role.GUARDIAN, 2L);
        Connection conn = connection(subject, guardian, null, ConnectionStatus.CONNECTED);
        given(userService.getByPhone("01022222222")).willReturn(guardian);
        given(connectionRepository.findByGuardianAndDeletedAtIsNull(guardian)).willReturn(List.of(conn));
        given(checkInService.getLatestCheckInBySubject(subject))
                .willReturn(new GetLatestCheckInRespDto(null, false));

        var resp = connectionService.getConnections("01022222222");

        assertThat(resp.getConnections()).hasSize(1);
        assertThat(resp.getConnections().get(0).getRequesterRole()).isNull();
    }

    // ─── deleteConnection ─────────────────────────────────────────────────────

    @Test
    void deleteConnection_당사자_본인연결_소프트삭제() {
        User subject = userWithId("01011111111", Role.SUBJECT, 1L);
        User guardian = userWithId("01022222222", Role.GUARDIAN, 2L);
        Connection conn = connection(subject, guardian, subject, ConnectionStatus.CONNECTED);
        given(userService.getByPhone("01011111111")).willReturn(subject);
        given(connectionRepository.findById(1L)).willReturn(Optional.of(conn));

        connectionService.deleteConnection(1L, "01011111111");

        assertThat(conn.getDeletedAt()).isNotNull();
    }

    @Test
    void deleteConnection_보호자_본인연결_소프트삭제() {
        User subject = userWithId("01011111111", Role.SUBJECT, 1L);
        User guardian = userWithId("01022222222", Role.GUARDIAN, 2L);
        Connection conn = connection(subject, guardian, subject, ConnectionStatus.CONNECTED);
        given(userService.getByPhone("01022222222")).willReturn(guardian);
        given(connectionRepository.findById(1L)).willReturn(Optional.of(conn));

        connectionService.deleteConnection(1L, "01022222222");

        assertThat(conn.getDeletedAt()).isNotNull();
    }

    @Test
    void deleteConnection_존재하지않는연결_예외발생() {
        User subject = user("01011111111", Role.SUBJECT);
        given(userService.getByPhone("01011111111")).willReturn(subject);
        given(connectionRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> connectionService.deleteConnection(99L, "01011111111"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.CONNECTION_NOT_FOUND);
    }

    @Test
    void deleteConnection_이미삭제된연결_예외발생() {
        User subject = userWithId("01011111111", Role.SUBJECT, 1L);
        User guardian = userWithId("01022222222", Role.GUARDIAN, 2L);
        Connection conn = connection(subject, guardian, subject, ConnectionStatus.PENDING);
        conn.softDelete();
        given(userService.getByPhone("01011111111")).willReturn(subject);
        given(connectionRepository.findById(1L)).willReturn(Optional.of(conn));

        assertThatThrownBy(() -> connectionService.deleteConnection(1L, "01011111111"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.CONNECTION_NOT_FOUND);
    }

    @Test
    void deleteConnection_관계없는사용자_예외발생() {
        User subject = userWithId("01011111111", Role.SUBJECT, 1L);
        User guardian = userWithId("01022222222", Role.GUARDIAN, 2L);
        User other = userWithId("01099999999", Role.SUBJECT, 3L);
        Connection conn = connection(subject, guardian, subject, ConnectionStatus.CONNECTED);
        given(userService.getByPhone("01099999999")).willReturn(other);
        given(connectionRepository.findById(1L)).willReturn(Optional.of(conn));

        assertThatThrownBy(() -> connectionService.deleteConnection(1L, "01099999999"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.CONNECTION_DELETE_FORBIDDEN);
    }

    // ─── updateConnectionName ──────────────────────────────────────────────────

    @Test
    void updateConnectionName_당사자_본인연결_이름수정() {
        User subject = userWithId("01011111111", Role.SUBJECT, 1L);
        User guardian = userWithId("01022222222", Role.GUARDIAN, 2L);
        Connection conn = connection(subject, guardian, subject, ConnectionStatus.PENDING);
        ReflectionTestUtils.setField(conn, "id", 1L);
        given(userService.getByPhone("01011111111")).willReturn(subject);
        given(connectionRepository.findById(1L)).willReturn(Optional.of(conn));

        connectionService.updateConnectionName(1L, "01011111111", updateNameReq("새이름"));

        assertThat(conn.getSubjectGivenName()).isEqualTo("새이름");
    }

    @Test
    void updateConnectionName_당사자_다른연결_예외발생() {
        User subject = userWithId("01011111111", Role.SUBJECT, 1L);
        User other = userWithId("01099999999", Role.SUBJECT, 2L);
        User guardian = userWithId("01022222222", Role.GUARDIAN, 3L);
        Connection conn = connection(other, guardian, other, ConnectionStatus.PENDING);
        given(userService.getByPhone("01011111111")).willReturn(subject);
        given(connectionRepository.findById(1L)).willReturn(Optional.of(conn));

        assertThatThrownBy(() -> connectionService.updateConnectionName(1L, "01011111111", updateNameReq("새이름")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.CONNECTION_FORBIDDEN);
    }

    @Test
    void updateConnectionName_보호자_본인연결_이름수정() {
        User subject = userWithId("01011111111", Role.SUBJECT, 1L);
        User guardian = userWithId("01022222222", Role.GUARDIAN, 2L);
        Connection conn = connection(subject, guardian, subject, ConnectionStatus.CONNECTED);
        given(userService.getByPhone("01022222222")).willReturn(guardian);
        given(connectionRepository.findById(1L)).willReturn(Optional.of(conn));

        connectionService.updateConnectionName(1L, "01022222222", updateNameReq("새이름"));

        assertThat(conn.getGuardianGivenName()).isEqualTo("새이름");
    }

    @Test
    void updateConnectionName_보호자_다른연결_예외발생() {
        User subject = userWithId("01011111111", Role.SUBJECT, 1L);
        User guardian = userWithId("01022222222", Role.GUARDIAN, 2L);
        User otherGuardian = userWithId("01033333333", Role.GUARDIAN, 3L);
        Connection conn = connection(subject, otherGuardian, subject, ConnectionStatus.CONNECTED);
        given(userService.getByPhone("01022222222")).willReturn(guardian);
        given(connectionRepository.findById(1L)).willReturn(Optional.of(conn));

        assertThatThrownBy(() -> connectionService.updateConnectionName(1L, "01022222222", updateNameReq("새이름")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.CONNECTION_FORBIDDEN);
    }
}
