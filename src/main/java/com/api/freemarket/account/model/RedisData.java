package com.api.freemarket.account.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RedisData {

    private Long memberNo;

    private String role;

    private String refreshToken;
}
