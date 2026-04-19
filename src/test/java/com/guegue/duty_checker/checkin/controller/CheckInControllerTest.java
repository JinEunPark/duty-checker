package com.guegue.duty_checker.checkin.controller;

import com.guegue.duty_checker.auth.infrastructure.RefreshTokenRedisRepository;
import com.guegue.duty_checker.auth.infrastructure.SmsCodeRedisRepository;
import com.guegue.duty_checker.auth.infrastructure.VerifiedPhoneRedisRepository;
import com.guegue.duty_checker.checkin.domain.CheckIn;
import com.guegue.duty_checker.checkin.dto.CreateCheckInRespDto;
import com.guegue.duty_checker.checkin.dto.GetLatestCheckInRespDto;
import com.guegue.duty_checker.checkin.service.CheckInService;
import com.guegue.duty_checker.user.domain.Role;
import com.guegue.duty_checker.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.ZonedDateTime;

import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class CheckInControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @MockitoBean
    private CheckInService checkInService;

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

    @Test
    void createCheckIn_인증없음_401반환() throws Exception {
        mockMvc.perform(post("/api/v1/check-ins"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getLatestCheckIn_인증없음_401반환() throws Exception {
        mockMvc.perform(get("/api/v1/check-ins/latest"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "01012345678")
    void createCheckIn_인증됨_201반환_직렬화검증() throws Exception {
        // Hibernate 바이트코드 강화로 인해 mock() 대신 실제 엔티티 빌더 + ReflectionTestUtils 사용
        User subject = User.builder().phone("01012345678").password("pw").role(Role.SUBJECT).build();
        CheckIn checkIn = CheckIn.builder().subject(subject).checkedAt(ZonedDateTime.now()).build();
        ReflectionTestUtils.setField(checkIn, "id", 1L);
        given(checkInService.createCheckIn(any())).willReturn(new CreateCheckInRespDto(checkIn));

        mockMvc.perform(post("/api/v1/check-ins"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.checkedAt").exists());
        // 500이면 ZonedDateTime Jackson 직렬화 설정 오류
    }

    @Test
    @WithMockUser(username = "01012345678")
    void getLatestCheckIn_인증됨_200반환() throws Exception {
        given(checkInService.getLatestCheckIn(any())).willReturn(new GetLatestCheckInRespDto(ZonedDateTime.now(), true));

        mockMvc.perform(get("/api/v1/check-ins/latest"))
                .andExpect(status().isOk());
    }
}
