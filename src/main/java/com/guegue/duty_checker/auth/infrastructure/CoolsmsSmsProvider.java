package com.guegue.duty_checker.auth.infrastructure;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("prod")
public class CoolsmsSmsProvider implements SmsProvider {

    @Value("${coolsms.api.key}")
    private String apiKey;

    @Value("${coolsms.api.secret.key}")
    private String apiSecretKey;

    @Value("${coolsms.api.domain}")
    private String domain;

    @Value("${coolsms.api.from-num}")
    private String fromNum;

    private DefaultMessageService messageService;

    @PostConstruct
    public void initialize() {
        this.messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecretKey, domain);
    }

    @Override
    public void send(String phone, String code) {
        Message message = new Message();
        message.setFrom(fromNum);
        message.setTo(phone);
        message.setText(String.format("[오늘, 안부] 인증번호 : %s", code));
        messageService.sendOne(new SingleMessageSendingRequest(message));
        log.info("[SMS] {} 으로 인증코드 발송 완료", phone);
    }
}
