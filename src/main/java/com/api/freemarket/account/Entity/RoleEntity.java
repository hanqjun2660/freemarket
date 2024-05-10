package com.api.freemarket.account.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "role")
public class RoleEntity {

    @Id()
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roleNo;

    @Column(name = "name")
    private String name;

    @Column(name = "member_no")
    private Long memberNo;
}
