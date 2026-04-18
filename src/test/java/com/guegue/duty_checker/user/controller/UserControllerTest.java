package com.guegue.duty_checker.user.controller;

import com.guegue.duty_checker.auth.infrastructure.RefreshTokenRedisRepository;
import com.guegue.duty_checker.auth.infrastructure.SmsCodeRedisRepository;
import com.guegue.duty_checker.auth.infrastructure.VerifiedPhoneRedisRepository;
import com.guegue.duty_checker.auth.service.AuthService;
import com.guegue.duty_checker.user.service.UserService;
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

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class UserControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

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

    @Test
    void updateDeviceToken_인증없음_401반환() throws Exception {
        mockMvc.perform(patch("/api/v1/users/device-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "fcmToken": "token123" }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_인증없음_401반환() throws Exception {
        mockMvc.perform(post("/api/v1/users/logout"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void withdraw_인증없음_401반환() throws Exception {
        mockMvc.perform(delete("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "01012345678")
    void updateDeviceToken_정상요청_200반환() throws Exception {
        mockMvc.perform(patch("/api/v1/users/device-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "fcmToken": "token123" }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "01012345678")
    void updateDeviceToken_빈토큰_400반환() throws Exception {
        mockMvc.perform(patch("/api/v1/users/device-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "fcmToken": "" }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "01012345678")
    void logout_정상요청_200반환() throws Exception {
        mockMvc.perform(post("/api/v1/users/logout"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "01012345678")
    void withdraw_정상요청_204반환() throws Exception {
        mockMvc.perform(delete("/api/v1/users/me"))
                .andExpect(status().isNoContent());
    }
}
