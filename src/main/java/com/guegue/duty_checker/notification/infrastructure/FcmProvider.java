package com.guegue.duty_checker.notification.infrastructure;

public interface FcmProvider {

    void send(String fcmToken, String title, String body);
}
