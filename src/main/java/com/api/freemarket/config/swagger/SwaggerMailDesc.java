package com.api.freemarket.config.swagger;

public final class SwaggerMailDesc {

    // 인증 메일 발송
    public static final String SEND_MAIL_SUCCESS_DESC = "성공시 200 코드 반환";
    public static final String SEND_MAIL_FAILED_DESC = "실패시 500 코드 반환";
    public static final String SEND_MAIL_EX_DESC = "발송 대상의 이메일 주소";
    public static final String SEND_MAIL_EX_VAL= """
            {
                "toEmail":"example@example.com"
            }
            """;

    // 인증 번호 검증
    public static final String VALID_MAIL_SUCCESS_DESC = "성공시 200 코드 반환";
    public static final String VALID_MAIL_FALIED_DESC = "실패시 500 코드 반환 및 검증 실패 메세지 반환";
    public static final String VALID_MAIL_NUMBER_EX_DESC = "이메일 인증 번호 필수";
    public static final String VALID_MAIL_NUMBER_EX_VAL = """
            {
                "eamil":"example@example.com",
                "certNo":"12345678"
            }
            """;
}
