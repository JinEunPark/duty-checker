package com.guegue.duty_checker.auth.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "sms_codes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SmsCode {

    private static final int MAX_SEND_COUNT = 3;
    private static final int BLOCK_MINUTES = 30;
    private static final int CODE_EXPIRE_MINUTES = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private int sendCount;

    @Column
    private LocalDateTime blockedUntil;

    @Column(nullable = false)
    private boolean verified;

    public static SmsCode create(String phoneNumber, String code) {
        SmsCode smsCode = new SmsCode();
        smsCode.phoneNumber = phoneNumber;
        smsCode.code = code;
        smsCode.expiresAt = LocalDateTime.now().plusMinutes(CODE_EXPIRE_MINUTES);
        smsCode.sendCount = 1;
        smsCode.verified = false;
        return smsCode;
    }

    public void resend(String newCode) {
        this.code = newCode;
        this.expiresAt = LocalDateTime.now().plusMinutes(CODE_EXPIRE_MINUTES);
        this.sendCount++;
        this.verified = false;

        if (this.sendCount >= MAX_SEND_COUNT) {
            this.blockedUntil = LocalDateTime.now().plusMinutes(BLOCK_MINUTES);
        }
    }

    public boolean isBlocked() {
        return blockedUntil != null && LocalDateTime.now().isBefore(blockedUntil);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean matches(String inputCode) {
        return this.code.equals(inputCode);
    }

    public void verify() {
        this.verified = true;
    }
}
