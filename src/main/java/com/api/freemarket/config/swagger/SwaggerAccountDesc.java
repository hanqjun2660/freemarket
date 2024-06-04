package com.api.freemarket.config.swagger;

public final class SwaggerAccountDesc {

    // 일반 회원 로그인
    public static final String NORMAL_USER_LOGIN_DESC = "일반 회원의 로그인 API";
    public static final String NORMAL_USER_LOGIN_SUCCESS_DESC = "성공시 200코드 반환";
    public static final String NORMAL_USER_LOGIN_FAILED_DESC = "실패시 500코드 반환";
    public static final String NORMAL_USER_LOGIN_EX_DESC = "아이디, 패스워드 JSON으로 전달";
    public static final String NORMAL_USER_LOGIN_EX_VAL = """
            {
                "memberId":"회원 아이디",
                "password":"비밀번호"
            }
            """;

    // 토큰 재발급
    public static final String TOKEN_REISSUE_DESC = "accessToken이 만료되었을때 Token 재발급을 위한 API, Cookie내 RefreshToken 필수";

    // 추가 정보 입력
    public static final String ADD_INFO_DESC = "소셜 회원 추가정보 입력(휴대폰 인증 선행 필수), memberNo 쿠키내 수동으로 추가해야함";
    public static final String ADD_INFO_EX_DESC = "휴대폰 번호만 json으로 전달(쿠키내 meberNo 존재해야함. 테스트시 수동으로 넣을것)";
    public static final String ADD_INFO_EX_VAL = """
            {
                "phone":"01022772660"
            }
            """;
}


;