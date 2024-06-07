package com.api.freemarket.domain.account.model;

import com.api.freemarket.common.validation.ValidationGroups;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.util.Date;

@Schema(description = "회원 정보 조회, 로그인, 수정용 DTO")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserDTO {

    private Long memberNo;

    @NotBlank(message = "이름은 필수 항목 입니다.", groups = {ValidationGroups.joinValidation.class})
    private String name;

    @NotBlank(message = "비밀번호는 필수 항목 입니다.", groups = {ValidationGroups.joinValidation.class, ValidationGroups.loginValidation.class})
    private String password;

    @NotBlank(groups = {ValidationGroups.joinValidation.class, ValidationGroups.NicknameValidation.class})
    private String nickname;

    private String profileImg;

    @NotBlank(message = "핸드폰 번호는 필수 항목 입니다.", groups = {ValidationGroups.addInfoValidation.class})
    private String phone;

    private String provider;

    @NotBlank(message = "이메일은 필수 항목 입니다.", groups = {ValidationGroups.joinValidation.class})
    @Email(message = "유효한 형식의 이메일이 아닙니다.", groups = {ValidationGroups.joinValidation.class})
    private String email;

    private String status;

    private Date joinDate;

    private String address;

    @NotBlank(message = "아이디는 필수 항목 입니다.", groups = {ValidationGroups.memberIdValidation.class, ValidationGroups.joinValidation.class, ValidationGroups.loginValidation.class})
    @Pattern(regexp = "^[a-zA-Z0-9]{7,11}$", message = "아이디는 영문과 숫자를 포함하여 7자리에서 11자리 사이여야 합니다.", groups = {ValidationGroups.memberIdValidation.class, ValidationGroups.joinValidation.class, ValidationGroups.loginValidation.class})
    private String memberId;

    private String role;

    private String registStatus;
}
