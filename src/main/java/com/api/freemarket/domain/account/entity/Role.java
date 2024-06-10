package com.api.freemarket.domain.account.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "role")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roleNo;

    @Column(name = "name")
    private String name;

    @Column(name = "member_no")
    private Long memberNo;
}
