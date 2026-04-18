package com.guegue.duty_checker.connection.controller;

import com.guegue.duty_checker.auth.infrastructure.RefreshTokenRedisRepository;
import com.guegue.duty_checker.auth.infrastructure.SmsCodeRedisRepository;
import com.guegue.duty_checker.auth.infrastructure.VerifiedPhoneRedisRepository;
import com.guegue.duty_checker.connection.domain.Connection;
import com.guegue.duty_checker.connection.domain.ConnectionStatus;
import com.guegue.duty_checker.connection.dto.ConnectionItemDto;
import com.guegue.duty_checker.connection.dto.GetConnectionsRespDto;
import com.guegue.duty_checker.connection.dto.UpdateConnectionNameRespDto;
import com.guegue.duty_checker.connection.dto.UpdateConnectionStatusRespDto;
import com.guegue.duty_checker.connection.service.ConnectionService;
import com.guegue.duty_checker.user.domain.Role;
import com.guegue.duty_checker.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class ConnectionControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @MockitoBean
    private ConnectionService connectionService;

    @MockitoBean
    private SmsCodeRedisRepository smsCodeRedisRepository;

    @MockitoBean
    private RefreshTokenRedisRepository refreshTokenRedisRepository;

    @MockitoBean
    private VerifiedPhoneRedisRepository verifiedPhoneRedisRepository;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    // ==================== 인증 검증 ====================

    @Test
    void getConnections_인증없음_401반환() throws Exception {
        mockMvc.perform(get("/api/v1/connections"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void addConnection_인증없음_401반환() throws Exception {
        mockMvc.perform(post("/api/v1/connections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "targetPhone": "01012345678" }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateConnectionStatus_인증없음_401반환() throws Exception {
        mockMvc.perform(patch("/api/v1/connections/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "status": "CONNECTED" }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateConnectionName_인증없음_401반환() throws Exception {
        mockMvc.perform(patch("/api/v1/connections/1/name")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "name": "홍길동" }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteConnection_인증없음_401반환() throws Exception {
        mockMvc.perform(delete("/api/v1/connections/1"))
                .andExpect(status().isUnauthorized());
    }

    // ==================== 정상 동작 및 직렬화 검증 ====================

    @Test
    @WithMockUser(username = "01012345678")
    void getConnections_인증됨_200반환_직렬화검증() throws Exception {
        // Hibernate 바이트코드 강화로 인해 mock() 대신 실제 엔티티 빌더 + ReflectionTestUtils 사용
        User subject = User.builder().phone("01099998888").password("pw").role(Role.SUBJECT).build();
        User requester = User.builder().phone("01012345678").password("pw").role(Role.GUARDIAN).build();
        Connection connection = Connection.builder()
                .subject(subject)
                .guardianPhone("01012345678")
                .requester(requester)
                .guardianGivenName("홍길동")
                .status(ConnectionStatus.CONNECTED)
                .build();
        ReflectionTestUtils.setField(connection, "id", 1L);

        ZonedDateTime now = ZonedDateTime.now();
        ConnectionItemDto item = ConnectionItemDto.forGuardian(connection, now, false);
        GetConnectionsRespDto resp = new GetConnectionsRespDto(Role.GUARDIAN, List.of(item));
        given(connectionService.getConnections(any())).willReturn(resp);

        mockMvc.perform(get("/api/v1/connections"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connections").isArray())
                .andExpect(jsonPath("$.connections[0].latestCheckedAt").exists())
                .andExpect(jsonPath("$.connections[0].requesterRole").exists());
        // 500이면 ZonedDateTime 또는 Role enum 직렬화 설정 오류
    }

    @Test
    @WithMockUser(username = "01012345678")
    void addConnection_정상요청_201반환() throws Exception {
        given(connectionService.addConnection(any(), any())).willReturn(null);

        mockMvc.perform(post("/api/v1/connections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "targetPhone": "01012345678" }
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "01012345678")
    void updateConnectionStatus_정상요청_200반환() throws Exception {
        given(connectionService.updateConnectionStatus(eq(1L), any(), any()))
                .willReturn(new UpdateConnectionStatusRespDto(1L, ConnectionStatus.CONNECTED));

        mockMvc.perform(patch("/api/v1/connections/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "status": "CONNECTED" }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "01012345678")
    void updateConnectionName_정상요청_200반환() throws Exception {
        User subject = User.builder().phone("01099998888").password("pw").role(Role.SUBJECT).build();
        User requester = User.builder().phone("01012345678").password("pw").role(Role.GUARDIAN).build();
        Connection connection = Connection.builder()
                .subject(subject)
                .guardianPhone("01012345678")
                .requester(requester)
                .guardianGivenName("홍길동")
                .status(ConnectionStatus.CONNECTED)
                .build();
        ReflectionTestUtils.setField(connection, "id", 1L);

        given(connectionService.updateConnectionName(eq(1L), any(), any()))
                .willReturn(UpdateConnectionNameRespDto.forGuardian(connection));

        mockMvc.perform(patch("/api/v1/connections/1/name")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "name": "홍길동" }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "01012345678")
    void deleteConnection_정상요청_204반환() throws Exception {
        mockMvc.perform(delete("/api/v1/connections/1"))
                .andExpect(status().isNoContent());
    }

    // ==================== 요청 검증 ====================

    @Test
    @WithMockUser(username = "01012345678")
    void addConnection_빈전화번호_400반환() throws Exception {
        mockMvc.perform(post("/api/v1/connections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "targetPhone": "" }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "01012345678")
    void updateConnectionStatus_status미전달_400반환() throws Exception {
        mockMvc.perform(patch("/api/v1/connections/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "01012345678")
    void updateConnectionStatus_문자열id_400반환() throws Exception {
        mockMvc.perform(patch("/api/v1/connections/abc/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "status": "CONNECTED" }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "01012345678")
    void updateConnectionName_문자열id_400반환() throws Exception {
        mockMvc.perform(patch("/api/v1/connections/abc/name")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "name": "홍길동" }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "01012345678")
    void deleteConnection_문자열id_400반환() throws Exception {
        mockMvc.perform(delete("/api/v1/connections/abc"))
                .andExpect(status().isBadRequest());
    }
}
