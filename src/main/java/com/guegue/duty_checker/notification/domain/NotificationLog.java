package com.guegue.duty_checker.notification.domain;

import com.guegue.duty_checker.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_logs",
        uniqueConstraints = @UniqueConstraint(columnNames = {"subject_id", "guardian_id", "notified_date"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private User subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guardian_id", nullable = false)
    private User guardian;

    @Column(nullable = false)
    private LocalDate notifiedDate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public NotificationLog(User subject, User guardian, LocalDate notifiedDate) {
        this.subject = subject;
        this.guardian = guardian;
        this.notifiedDate = notifiedDate;
        this.createdAt = LocalDateTime.now();
    }
}
