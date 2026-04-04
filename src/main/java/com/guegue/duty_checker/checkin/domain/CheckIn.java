package com.guegue.duty_checker.checkin.domain;

import com.guegue.duty_checker.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Entity
@Table(name = "check_ins")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CheckIn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private User subject;

    @Column(nullable = false)
    private ZonedDateTime checkedAt;

    @Builder
    public CheckIn(User subject, ZonedDateTime checkedAt) {
        this.subject = subject;
        this.checkedAt = checkedAt;
    }
}
