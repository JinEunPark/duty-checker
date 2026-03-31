package com.guegue.duty_checker.auth.infrastructure;

public interface SmsProvider {
    void send(String phone, String code);
}
