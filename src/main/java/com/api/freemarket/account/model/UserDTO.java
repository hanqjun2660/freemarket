package com.api.freemarket.account.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Setter
@Getter
@ToString
public class UserDTO {

    private Long memberNo;

    @NotNull(message = "이름은 필수 항목 입니다.")
    private String name;

    @NotNull(message = "비밀번호는 필수 항목 입니다.")
    private String password;

    private String nickname;

    private String profileImg;

    @NotNull(message = "핸드폰 번호는 필수 항목 입니다.")
    private String phone;

    private String provider;

    @NotNull(message = "이메일은 필수 항목 입니다.")
    @Email(message = "유효한 형식의 이메일이 아닙니다.")
    private String email;

    private String status;

    private Date joinDate;

    private String address;

    @NotNull(message = "아이디는 필수 항목 입니다.")
    @Pattern(regexp = "^[a-zA-Z0-9]{7,11}$", message = "아이디는 영문과 숫자를 포함하여 7자리에서 11자리 사이여야 합니다.")
    private String memberId;

    private String role;
}
