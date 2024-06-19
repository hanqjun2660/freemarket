package com.api.freemarket.domain.account.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Schema(description = "비밀번호 찾기용 요청 모델")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FindPasswordRequest {

    @NotBlank(message = "아이디는 필수 입력 항목입니다.")
    private String memberId;

    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    private String email;

    private String emailTitle;

    private String emailText;
}
