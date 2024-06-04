package com.api.freemarket.domain.account.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

import java.util.Date;

@Entity
@Setter
@Getter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@DynamicUpdate
@Table(name = "member")
public class User {

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

    @Column(name = "regist_status")
    private String registStatus;

    @PrePersist
    protected void onCreate() {
        if(registStatus == null) {
            registStatus = "N";
        }
    }
}
