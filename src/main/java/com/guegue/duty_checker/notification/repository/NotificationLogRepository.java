package com.guegue.duty_checker.notification.repository;

import com.guegue.duty_checker.notification.domain.NotificationLog;
import com.guegue.duty_checker.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

    boolean existsBySubjectAndGuardianAndNotifiedDate(User subject, User guardian, LocalDate notifiedDate);
}
