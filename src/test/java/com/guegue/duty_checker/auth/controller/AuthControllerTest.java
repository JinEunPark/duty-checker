package com.guegue.duty_checker.auth.controller;

import com.guegue.duty_checker.auth.dto.CheckPhoneRespDto;
import com.guegue.duty_checker.auth.dto.LoginRespDto;
import com.guegue.duty_checker.auth.dto.RefreshTokenRespDto;
import com.guegue.duty_checker.auth.dto.RegisterRespDto;
import com.guegue.duty_checker.auth.dto.SendCodeRespDto;
import com.guegue.duty_checker.auth.infrastructure.RefreshTokenRedisRepository;
import com.guegue.duty_checker.auth.infrastructure.SmsCodeRedisRepository;
import com.guegue.duty_checker.auth.infrastructure.VerifiedPhoneRedisRepository;
import com.guegue.duty_checker.auth.service.AuthService;
import com.guegue.duty_checker.user.domain.Role;
import com.guegue.duty_checker.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.ZonedDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class AuthControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

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

    private User buildUser(Long id, String phone, Role role) {
        User user = User.builder()
                .phone(phone)
                .password("encoded")
                .role(role)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    @Test
    void checkPhone_유효한전화번호_200반환() throws Exception {
        given(authService.checkPhone("01012345678")).willReturn(new CheckPhoneRespDto(true));

        mockMvc.perform(get("/api/v1/auth/check-phone")
                        .param("phone", "01012345678"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists").exists());
    }

    @Test
    void checkPhone_잘못된형식_400반환() throws Exception {
        mockMvc.perform(get("/api/v1/auth/check-phone")
                        .param("phone", "invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sendCode_정상요청_200반환() throws Exception {
        given(authService.sendCode(any())).willReturn(new SendCodeRespDto(ZonedDateTime.now()));

        mockMvc.perform(post("/api/v1/auth/send-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "phone": "01012345678" }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void sendCode_빈전화번호_400반환() throws Exception {
        mockMvc.perform(post("/api/v1/auth/send-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "phone": "" }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void verifyCode_정상요청_200반환() throws Exception {
        mockMvc.perform(post("/api/v1/auth/verify-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "phone": "01012345678", "verificationCode": "123456" }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void register_정상요청_201반환() throws Exception {
        User user = buildUser(1L, "01012345678", Role.SUBJECT);
        given(authService.register(any())).willReturn(new RegisterRespDto(user));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "phone": "01012345678", "password": "password123", "role": "SUBJECT" }
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void login_정상요청_200반환_직렬화검증() throws Exception {
        User user = buildUser(1L, "01012345678", Role.SUBJECT);
        given(authService.login(any())).willReturn(new LoginRespDto("access", "refresh", user));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "phone": "01012345678", "password": "pw" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.user.role").exists());
    }

    @Test
    void refresh_정상요청_200반환() throws Exception {
        given(authService.refresh(any())).willReturn(new RefreshTokenRespDto("newAccessToken", "newRefreshToken"));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "refreshToken": "token" }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void changePassword_정상요청_200반환() throws Exception {
        mockMvc.perform(patch("/api/v1/auth/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "phone": "01012345678", "newPassword": "newpass123" }
                                """))
                .andExpect(status().isOk());
    }
}
