package com.guegue.duty_checker.checkin.repository;

import com.guegue.duty_checker.checkin.domain.CheckIn;
import com.guegue.duty_checker.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.ZonedDateTime;
import java.util.Optional;

public interface CheckInRepository extends JpaRepository<CheckIn, Long> {

    boolean existsBySubjectAndCheckedAtBetween(User subject, ZonedDateTime start, ZonedDateTime end);

    Optional<CheckIn> findTopBySubjectOrderByCheckedAtDesc(User subject);

    void deleteBySubject(User subject);
}
