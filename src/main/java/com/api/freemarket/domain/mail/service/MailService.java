package com.api.freemarket.domain.mail.service;

import com.api.freemarket.common.email.EmailUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender emailSender;
    private final EmailUtil emailUtil;

    public void sendEmail(SimpleMailMessage emailForm) {
        try {
            emailSender.send(emailForm);
        } catch (MailException e) {
            e.printStackTrace();
            log.info("mail service exception");
            throw new MailSendException(e.getMessage());
        }
    }

    public void sendTemplateEmail(String title, String toEmail, Map<String, String> dataMap) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            emailUtil.settingsTemplateEmailForm(message, title, toEmail, dataMap);
            emailSender.send(message);
        } catch (MailException e) {
            log.info("MailException");
            throw new MailSendException(e.getMessage());
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}
