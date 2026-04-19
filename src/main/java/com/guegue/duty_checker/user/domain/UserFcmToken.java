package com.guegue.duty_checker.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_fcm_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserFcmToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true, length = 200)
    private String token;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public UserFcmToken(User user, String token) {
        this.user = user;
        this.token = token;
        this.createdAt = LocalDateTime.now();
    }
}
