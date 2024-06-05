package com.api.freemarket.config.swagger;

public final class SwaggerMailDesc {

    public static final String SEND_MAIL_SUCCESS_DESC = "성공시 200 코드 반환";
    public static final String SEND_MAIL_FAILED_DESC = "성공시 500 코드 반환";
    public static final String SEND_MAIL_EX_DESC = "발송 대상의 이메일 주소";
    public static final String SEND_MAIL_EX_VAL= """
            {
                "toEmail":"example@example.com"
            }
            """;
}
