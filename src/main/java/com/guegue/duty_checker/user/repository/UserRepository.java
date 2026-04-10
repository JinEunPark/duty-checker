package com.guegue.duty_checker.user.repository;

import com.guegue.duty_checker.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByPhone(String phone);

    boolean existsByPhone(String phone);

    Optional<User> findByPhoneAndDeletedAtIsNull(String phone);

    boolean existsByPhoneAndDeletedAtIsNull(String phone);
}
