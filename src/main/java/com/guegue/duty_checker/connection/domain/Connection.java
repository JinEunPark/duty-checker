package com.guegue.duty_checker.connection.domain;

import com.guegue.duty_checker.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "connections")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Connection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private User subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guardian_id")
    private User guardian;

    @Column(nullable = false, length = 20)
    private String guardianPhone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @Column(length = 20)
    private String subjectGivenName;

    @Column(length = 20)
    private String guardianGivenName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConnectionStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime deletedAt;

    @Builder
    public Connection(User subject, User guardian, String guardianPhone, User requester,
                      String subjectGivenName, String guardianGivenName,
                      ConnectionStatus status) {
        this.subject = subject;
        this.guardian = guardian;
        this.guardianPhone = guardianPhone;
        this.requester = requester;
        this.subjectGivenName = subjectGivenName;
        this.guardianGivenName = guardianGivenName;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public void updateSubjectGivenName(String name) {
        this.subjectGivenName = name;
    }

    public void updateGuardianGivenName(String name) {
        this.guardianGivenName = name;
    }

    public void updateStatus(ConnectionStatus status) {
        this.status = status;
    }

    public void connectGuardian(User guardian) {
        this.guardian = guardian;
        this.status = ConnectionStatus.CONNECTED;
    }
}
