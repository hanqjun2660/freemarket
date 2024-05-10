package com.api.freemarket.account.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Setter
@Getter
@ToString
public class UserDTO {

    private Long memberNo;

    private String name;

    private String password;

    private String nickname;

    private String profileImg;

    private String phone;

    private String provider;

    private String email;

    private String status;

    private Date joinDate;

    private String address;

    private String memberId;

    private String role;
}
