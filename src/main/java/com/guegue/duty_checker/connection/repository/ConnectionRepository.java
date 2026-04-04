package com.guegue.duty_checker.connection.repository;

import com.guegue.duty_checker.connection.domain.Connection;
import com.guegue.duty_checker.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConnectionRepository extends JpaRepository<Connection, Long> {

    List<Connection> findBySubject(User subject);

    List<Connection> findByGuardian(User guardian);

    List<Connection> findByStatus(com.guegue.duty_checker.connection.domain.ConnectionStatus status);
}
