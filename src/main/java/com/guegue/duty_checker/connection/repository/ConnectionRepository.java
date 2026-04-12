package com.guegue.duty_checker.connection.repository;

import com.guegue.duty_checker.connection.domain.Connection;
import com.guegue.duty_checker.connection.domain.ConnectionStatus;
import com.guegue.duty_checker.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConnectionRepository extends JpaRepository<Connection, Long> {

    List<Connection> findBySubjectAndDeletedAtIsNull(User subject);

    List<Connection> findByGuardianAndDeletedAtIsNull(User guardian);

    List<Connection> findByStatus(ConnectionStatus status);

    boolean existsBySubjectAndGuardianAndStatusInAndDeletedAtIsNull(
            User subject, User guardian, List<ConnectionStatus> statuses);

    void deleteBySubjectOrGuardian(User subject, User guardian);
}
