package com.api.freemarket.domain.mail.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CertNumberSendRequest {

    @NotEmpty(message = "이메일은 필수 항목 입니다.")
    @Email(message = "유효한 형식의 이메일이 아닙니다.")
    private String toEmail;
}
