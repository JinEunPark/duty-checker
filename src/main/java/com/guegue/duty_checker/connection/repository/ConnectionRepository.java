package com.guegue.duty_checker.connection.repository;

import com.guegue.duty_checker.connection.domain.Connection;
import com.guegue.duty_checker.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConnectionRepository extends JpaRepository<Connection, Long> {

    List<Connection> findBySubjectAndDeletedAtIsNull(User subject);

    List<Connection> findByGuardianAndDeletedAtIsNull(User guardian);

    List<Connection> findByStatus(com.guegue.duty_checker.connection.domain.ConnectionStatus status);

    boolean existsBySubjectAndGuardianPhoneAndDeletedAtIsNull(User subject, String guardianPhone);

    long countBySubjectAndDeletedAtIsNull(User subject);

    List<Connection> findByGuardianPhoneAndStatusAndDeletedAtIsNull(String guardianPhone, com.guegue.duty_checker.connection.domain.ConnectionStatus status);

    void deleteBySubjectOrGuardian(User subject, User guardian);
}
