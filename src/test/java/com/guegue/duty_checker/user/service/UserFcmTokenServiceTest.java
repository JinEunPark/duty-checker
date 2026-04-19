package com.guegue.duty_checker.user.service;

import com.guegue.duty_checker.user.domain.Role;
import com.guegue.duty_checker.user.domain.User;
import com.guegue.duty_checker.user.domain.UserFcmToken;
import com.guegue.duty_checker.user.repository.UserFcmTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserFcmTokenServiceTest {

    @InjectMocks UserFcmTokenService userFcmTokenService;

    @Mock UserFcmTokenRepository userFcmTokenRepository;

    private User user(String phone) {
        return User.builder().phone(phone).password("pw").role(Role.GUARDIAN).build();
    }

    private UserFcmToken token(User user, String tokenValue) {
        UserFcmToken t = UserFcmToken.builder().user(user).token(tokenValue).build();
        ReflectionTestUtils.setField(t, "id", 1L);
        return t;
    }

    @Test
    void saveToken_신규토큰_저장() {
        User user = user("01011111111");
        given(userFcmTokenRepository.existsByUserAndToken(user, "new-token")).willReturn(false);

        userFcmTokenService.saveToken(user, "new-token");

        ArgumentCaptor<UserFcmToken> captor = ArgumentCaptor.forClass(UserFcmToken.class);
        verify(userFcmTokenRepository).save(captor.capture());
        assertThat(captor.getValue().getToken()).isEqualTo("new-token");
        assertThat(captor.getValue().getUser()).isEqualTo(user);
    }

    @Test
    void saveToken_중복토큰_저장스킵() {
        User user = user("01011111111");
        given(userFcmTokenRepository.existsByUserAndToken(user, "existing-token")).willReturn(true);

        userFcmTokenService.saveToken(user, "existing-token");

        verify(userFcmTokenRepository, never()).save(any());
    }

    @Test
    void getTokensByUser_토큰목록반환() {
        User user = user("01011111111");
        given(userFcmTokenRepository.findAllByUser(user))
                .willReturn(List.of(token(user, "token-a"), token(user, "token-b")));

        List<String> tokens = userFcmTokenService.getTokensByUser(user);

        assertThat(tokens).containsExactly("token-a", "token-b");
    }

    @Test
    void getTokensByUser_토큰없음_빈목록반환() {
        User user = user("01011111111");
        given(userFcmTokenRepository.findAllByUser(user)).willReturn(List.of());

        List<String> tokens = userFcmTokenService.getTokensByUser(user);

        assertThat(tokens).isEmpty();
    }

    @Test
    void deleteAllByUser_유저토큰전체삭제() {
        User user = user("01011111111");

        userFcmTokenService.deleteAllByUser(user);

        verify(userFcmTokenRepository).deleteAllByUser(user);
    }
}
