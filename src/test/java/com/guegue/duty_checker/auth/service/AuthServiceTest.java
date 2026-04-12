package com.guegue.duty_checker.auth.service;

import com.guegue.duty_checker.auth.dto.*;
import com.guegue.duty_checker.auth.infrastructure.*;
import com.guegue.duty_checker.common.config.JwtProvider;
import com.guegue.duty_checker.common.exception.BusinessException;
import com.guegue.duty_checker.common.exception.ErrorCode;
import com.guegue.duty_checker.checkin.service.CheckInService;
import com.guegue.duty_checker.connection.service.ConnectionService;
import com.guegue.duty_checker.user.domain.Role;
import com.guegue.duty_checker.user.domain.User;
import com.guegue.duty_checker.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks AuthService authService;

    @Mock SmsCodeRedisRepository smsCodeRedisRepository;
    @Mock VerifiedPhoneRedisRepository verifiedPhoneRedisRepository;
    @Mock RefreshTokenRedisRepository refreshTokenRedisRepository;
    @Mock SmsProvider smsProvider;
    @Mock JwtProvider jwtProvider;
    @Mock UserService userService;
    @Mock ConnectionService connectionService;
    @Mock CheckInService checkInService;
    @Mock PasswordEncoder passwordEncoder;

    private SendCodeReqDto sendCodeReq(String phone) {
        SendCodeReqDto dto = new SendCodeReqDto();
        ReflectionTestUtils.setField(dto, "phone", phone);
        return dto;
    }

    private VerifyCodeReqDto verifyCodeReq(String phone, String code) {
        VerifyCodeReqDto dto = new VerifyCodeReqDto();
        ReflectionTestUtils.setField(dto, "phone", phone);
        ReflectionTestUtils.setField(dto, "verificationCode", code);
        return dto;
    }

    private RegisterReqDto registerReq(String phone, String password, Role role) {
        RegisterReqDto dto = new RegisterReqDto();
        ReflectionTestUtils.setField(dto, "phone", phone);
        ReflectionTestUtils.setField(dto, "password", password);
        ReflectionTestUtils.setField(dto, "role", role);
        return dto;
    }

    private LoginReqDto loginReq(String phone, String password) {
        LoginReqDto dto = new LoginReqDto();
        ReflectionTestUtils.setField(dto, "phone", phone);
        ReflectionTestUtils.setField(dto, "password", password);
        return dto;
    }

    private User user(String phone, Role role) {
        return User.builder().phone(phone).password("encoded").role(role).build();
    }

    // ─── checkPhone ────────────────────────────────────────────────────────

    @Test
    void checkPhone_가입된번호_exists참() {
        given(userService.existsByPhone("01011111111")).willReturn(true);

        CheckPhoneRespDto resp = authService.checkPhone("01011111111");

        assertThat(resp.isExists()).isTrue();
    }

    @Test
    void checkPhone_미가입번호_exists거짓() {
        given(userService.existsByPhone("01099999999")).willReturn(false);

        CheckPhoneRespDto resp = authService.checkPhone("01099999999");

        assertThat(resp.isExists()).isFalse();
    }

    // ─── sendCode ──────────────────────────────────────────────────────────

    @Test
    void sendCode_쿨다운중_예외발생() {
        given(smsCodeRedisRepository.isOnCooldown("01011111111")).willReturn(true);
        given(smsCodeRedisRepository.getRemainingCooldownSeconds("01011111111")).willReturn(30L);

        assertThatThrownBy(() -> authService.sendCode(sendCodeReq("01011111111")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.AUTH_SEND_CODE_COOLDOWN);
    }

    @Test
    void sendCode_정상_코드저장및SMS발송() {
        given(smsCodeRedisRepository.isOnCooldown("01011111111")).willReturn(false);

        SendCodeRespDto resp = authService.sendCode(sendCodeReq("01011111111"));

        verify(smsCodeRedisRepository).saveCode(eq("01011111111"), anyString());
        verify(smsProvider).send(eq("01011111111"), anyString());
        assertThat(resp.getExpiredAt()).isNotNull();
    }

    // ─── verifyCode ────────────────────────────────────────────────────────

    @Test
    void verifyCode_항상성공_인증저장() {
        authService.verifyCode(verifyCodeReq("01011111111", "123456"));

        verify(verifiedPhoneRedisRepository).save("01011111111");
    }

    // ─── register ──────────────────────────────────────────────────────────

    @Test
    void register_이미가입된번호_예외발생() {
        given(userService.existsByPhone("01011111111")).willReturn(true);

        assertThatThrownBy(() -> authService.register(registerReq("01011111111", "pw", Role.SUBJECT)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ALREADY_REGISTERED);
    }

    @Test
    void register_정상_유저저장() {
        given(userService.existsByPhone("01011111111")).willReturn(false);
        given(passwordEncoder.encode("pw")).willReturn("encoded");

        RegisterRespDto resp = authService.register(registerReq("01011111111", "pw", Role.SUBJECT));

        verify(userService).save(any(User.class));
        assertThat(resp).isNotNull();
    }

    // ─── login ─────────────────────────────────────────────────────────────

    @Test
    void login_비밀번호불일치_예외발생() {
        User u = user("01011111111", Role.SUBJECT);
        given(userService.getByPhone("01011111111")).willReturn(u);
        given(passwordEncoder.matches("wrong", "encoded")).willReturn(false);

        assertThatThrownBy(() -> authService.login(loginReq("01011111111", "wrong")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_PASSWORD);
    }

    @Test
    void login_정상_토큰발급() {
        User u = user("01011111111", Role.SUBJECT);
        given(userService.getByPhone("01011111111")).willReturn(u);
        given(passwordEncoder.matches("pw", "encoded")).willReturn(true);
        given(jwtProvider.generateAccessToken("01011111111")).willReturn("access");
        given(jwtProvider.generateRefreshToken("01011111111")).willReturn("refresh");

        LoginRespDto resp = authService.login(loginReq("01011111111", "pw"));

        assertThat(resp.getAccessToken()).isEqualTo("access");
        assertThat(resp.getRefreshToken()).isEqualTo("refresh");
        verify(refreshTokenRedisRepository).save("refresh", "01011111111");
    }

    // ─── refresh ───────────────────────────────────────────────────────────

    @Test
    void refresh_유효하지않은토큰_예외발생() {
        RefreshTokenReqDto dto = new RefreshTokenReqDto();
        ReflectionTestUtils.setField(dto, "refreshToken", "invalid");
        given(refreshTokenRedisRepository.findPhoneByToken("invalid")).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN);
    }

    @Test
    void refresh_정상_새토큰발급() {
        RefreshTokenReqDto dto = new RefreshTokenReqDto();
        ReflectionTestUtils.setField(dto, "refreshToken", "old-refresh");
        given(refreshTokenRedisRepository.findPhoneByToken("old-refresh")).willReturn(Optional.of("01011111111"));
        given(jwtProvider.generateAccessToken("01011111111")).willReturn("new-access");
        given(jwtProvider.generateRefreshToken("01011111111")).willReturn("new-refresh");

        RefreshTokenRespDto resp = authService.refresh(dto);

        verify(refreshTokenRedisRepository).deleteByToken("old-refresh");
        assertThat(resp.getAccessToken()).isEqualTo("new-access");
        assertThat(resp.getRefreshToken()).isEqualTo("new-refresh");
    }

    // ─── withdraw ──────────────────────────────────────────────────────────

    @Test
    void withdraw_정상_리프레시토큰삭제및연관데이터정리및유저삭제() {
        User u = user("01011111111", Role.SUBJECT);
        given(userService.getByPhone("01011111111")).willReturn(u);

        authService.withdraw("01011111111");

        verify(refreshTokenRedisRepository).deleteByPhone("01011111111");
        verify(connectionService).deleteAllByUser(u);
        verify(checkInService).deleteAllByUser(u);
        verify(userService).deleteUser("01011111111");
    }

    @Test
    void withdraw_존재하지않는사용자_예외발생() {
        given(userService.getByPhone("01099999999"))
                .willThrow(new BusinessException(ErrorCode.USER_NOT_FOUND));

        assertThatThrownBy(() -> authService.withdraw("01099999999"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }
}
