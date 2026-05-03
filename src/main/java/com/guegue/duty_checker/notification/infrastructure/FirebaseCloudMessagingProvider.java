package com.guegue.duty_checker.notification.infrastructure;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@Primary
public class FirebaseCloudMessagingProvider implements FcmProvider {

    private final FirebaseApp firebaseApp;

    public FirebaseCloudMessagingProvider(Optional<FirebaseApp> firebaseApp) {
        this.firebaseApp = firebaseApp.orElse(null);
        if (this.firebaseApp == null) {
            log.warn("FirebaseApp bean not found — FCM sends will be no-ops");
        }
    }

    @Override
    public void send(String fcmToken, String title, String body) {
        if (firebaseApp == null) {
            log.info("[MockFCM fallback] token={}, title={}, body={}", fcmToken, title, body);
            return;
        }
        if (fcmToken == null || fcmToken.isBlank()) {
            log.warn("Skip FCM send: fcmToken is empty");
            return;
        }
        Message message = buildMessage(fcmToken, title, body);
        try {
            String response = FirebaseMessaging.getInstance(firebaseApp).send(message);
            log.info("FCM sent: messageId={}", response);
        } catch (FirebaseMessagingException e) {
            log.error("FCM send failed: token={}, errorCode={}, message={}",
                fcmToken, e.getMessagingErrorCode(), e.getMessage());
            // 호출 흐름을 막지 않기 위해 예외를 다시 던지지 않는다.
        }
    }

    private Message buildMessage(String fcmToken, String title, String body) {
        return Message.builder()
            .setToken(fcmToken)
            .setNotification(Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build())
            .build();
    }
}
