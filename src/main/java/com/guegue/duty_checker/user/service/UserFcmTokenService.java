package com.guegue.duty_checker.user.service;

import com.guegue.duty_checker.user.domain.User;
import com.guegue.duty_checker.user.domain.UserFcmToken;
import com.guegue.duty_checker.user.repository.UserFcmTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserFcmTokenService {

    private final UserFcmTokenRepository userFcmTokenRepository;

    @Transactional
    public void saveToken(User user, String token) {
        if (!userFcmTokenRepository.existsByUserAndToken(user, token)) {
            userFcmTokenRepository.save(UserFcmToken.builder()
                    .user(user)
                    .token(token)
                    .build());
        }
    }

    @Transactional(readOnly = true)
    public List<String> getTokensByUser(User user) {
        return userFcmTokenRepository.findAllByUser(user)
                .stream()
                .map(UserFcmToken::getToken)
                .toList();
    }

    @Transactional
    public void deleteAllByUser(User user) {
        userFcmTokenRepository.deleteAllByUser(user);
    }
}
