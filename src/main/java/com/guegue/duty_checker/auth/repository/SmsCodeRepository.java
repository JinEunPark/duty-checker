package com.guegue.duty_checker.auth.repository;

import com.guegue.duty_checker.auth.domain.SmsCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SmsCodeRepository extends JpaRepository<SmsCode, Long> {

    Optional<SmsCode> findByPhoneNumber(String phoneNumber);
}
