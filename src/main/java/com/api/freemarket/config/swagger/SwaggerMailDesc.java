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
    public static final String VALID_MAIL_DESC = """
            인증 번호를 검증하는 API
            - 성공시 data 내 존재하는 'verify'의 값을 '/api/v1/account/find-password/temp-password'(임시 비밀번호 발급)요청시 함께 보내야함.
            """;
    public static final String VALID_MAIL_SUCCESS_DESC = "성공시 200 코드 반환";
    public static final String VALID_MAIL_SUCCESS_EX_VAL = """
            {
                "statusCode":"200",
                "message":"null",
                "data": {
                    "verify":"Y"
                }
            }
            """;
    public static final String VALID_MAIL_FALIED_DESC = "실패시 500 코드 반환 및 검증 실패 메세지 반환";
    public static final String VALID_MAIL_FALIED_EX_VAL = """
            {
                "statusCode":"500",
                "message":"실패 메세지",
                "data": {
                    "verify":"N"
                }
            }
            """;
    public static final String VALID_MAIL_NUMBER_EX_DESC = "이메일 인증 번호 필수";
    public static final String VALID_MAIL_NUMBER_EX_VAL = """
            {
                "email":"example@example.com",
                "certNo":"12345678"
            }
            """;
}
