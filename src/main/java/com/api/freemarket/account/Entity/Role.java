package com.api.freemarket.account.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Table(name = "role")
public class Role {

    @Id()
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roleNo;

    @Column(name = "name")
    private String name;

    @Column(name = "member_no")
    private Long memberNo;
}
