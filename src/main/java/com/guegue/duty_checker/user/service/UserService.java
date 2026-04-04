package com.guegue.duty_checker.user.service;

import com.guegue.duty_checker.common.exception.BusinessException;
import com.guegue.duty_checker.common.exception.ErrorCode;
import com.guegue.duty_checker.user.domain.User;
import com.guegue.duty_checker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User getByPhone(String phone) {
        return userRepository.findByPhone(phone)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
