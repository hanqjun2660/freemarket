package com.api.freemarket.domain.mail.service;

import com.api.freemarket.common.email.EmailUtil;
import com.api.freemarket.domain.account.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender emailSender;

    public void sendEmail(SimpleMailMessage emailForm) {
        try {
            emailSender.send(emailForm);
        } catch (MailException e) {
            e.printStackTrace();
            log.info("mail service exception");
            throw new MailSendException(e.getMessage());
        }
    }
}
