package com.api.freemarket.domain.mail.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Schema(description = "인증번호 검증요청 DTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class VaildCertNumberRequest {

    @NotEmpty(message = "이메일은 필수항목입니다.")
    @Email(message = "유효한 형식의 이메일이 아닙니다.")
    private String email;
    @NotEmpty(message = "인증번호는 필수항목입니다.")
    @Pattern(regexp = "^[0-9]{8}$", message = "인증번호는 8자리여야 합니다.")
    private String certNo;
}
