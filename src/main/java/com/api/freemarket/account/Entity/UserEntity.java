package com.api.freemarket.account.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "member")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberNo;

    @Column(name = "name")
    private String name;

    @Column(name = "password")
    private String password;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "profile_img")
    private String profileImg;

    @Column(name = "phone")
    private String phone;

    @Column(name = "provider")
    private String provider;

    @Column(name = "email")
    private String email;

    @Column(name = "status")
    private String status;

    @Column(name = "join_date")
    private Date joinDate;

    @Column(name = "address")
    private String address;

    @Column(name = "member_id")
    private String memberId;
}
