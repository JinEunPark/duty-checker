package com.guegue.duty_checker.user.repository;

import com.guegue.duty_checker.user.domain.User;
import com.guegue.duty_checker.user.domain.UserFcmToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserFcmTokenRepository extends JpaRepository<UserFcmToken, Long> {

    List<UserFcmToken> findAllByUser(User user);

    boolean existsByUserAndToken(User user, String token);

    void deleteAllByUser(User user);
}
