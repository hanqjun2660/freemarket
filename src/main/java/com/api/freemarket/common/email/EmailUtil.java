package com.api.freemarket.common.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class EmailUtil {

    private final TemplateEngine templateEngine;

    public String createCode() {
        int length = 8;
        try {
            Random random = SecureRandom.getInstanceStrong();
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < length; i++) {
                sb.append(random.nextInt(10));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public String createTempPassword() {
        int length = 10;
        String charSet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        try {
            Random random = SecureRandom.getInstanceStrong();
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < length; i++) {
                int index = random.nextInt(charSet.length());
                sb.append(charSet.charAt(index));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public SimpleMailMessage createEmailForm(String toEmail, String title, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(title);
        message.setText(text);

        return message;
    }

    /**
     * @param message -> MimeMessage 생성해서 전달 줘야함
     * @param title -> 메일의 제목
     * @param to -> 수신자 이메일 주소
     * @param dataMap -> 인증번호 등 메일에 포함할 내용을 전달(현재는 인증번호(CertNum)만 메일 본문에 포함되어있음 -> resources/template/email-template.html)
     * @throws MessagingException
     */
    public void settingsTemplateEmailForm(MimeMessage message, String title, String to, Map<String, String> dataMap) throws MessagingException {
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setSubject(title);    // 제목
        helper.setTo(to);            // 수신자

        Context context = new Context();
        dataMap.forEach((key, value) -> {
            context.setVariable(key, value);
        });

        String html = templateEngine.process("email-template", context);
        helper.setText(html, true);

        helper.addInline("image", new ClassPathResource("static/img/logo.png"));
    }
}
