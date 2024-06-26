package com.api.freemarket.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CommonResponseCode {
    OK("200", "ok"),
    REDIRECTION("300", "redirection"),
    ERROR("500", "error");

    private final String code;
    private final String defaultMessage;
}
