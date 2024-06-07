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
    public static final String ADD_INFO_DESC = "소셜 회원 추가정보 입력(휴대폰 인증 선행 필수), 테스트시 memberNo 쿠키 내 수동으로 추가해야함";
    public static final String ADD_INFO_EX_DESC = "휴대폰 번호만 json으로 전달(쿠키내 meberNo 존재해야함. 테스트시 수동으로 넣을것)";
    public static final String ADD_INFO_EX_VAL = """
            {
                "phone":"01022772660"
            }
            """;

    // 회원가입
    public static final String JOIN_DESC = "회원가입 API";
    public static final String JOIN_SUCCESS_DESC = "성공시 200코드 반환";
    public static final String JOIN_FAILED_DESC = "실패시 500코드 반환";
    public static final String JOIN_EX_DESC = "아이디,비밀번호,이름,별명,이메일 JSON으로 전달";
    public static final String JOIN_EX_VAL = """
            {
                "memberId":"회원 아이디",
                "password":"비밀번호",
                "name":"이름",
                "nickname":"별명",
                "email":"abcd@gmail.com"
            }
            """;

    // 별명 중복 체크
    public static final String CHECK_NICKNAME_DESC = "별명 중복 체크 API";
    public static final String CHECK_NICKNAME_SUCCESS_DESC = "성공시 200코드 반환";
    public static final String CHECK_NICKNAME_FAILED_DESC = "실패시 500코드 반환";
    public static final String CHECK_NICKNAME_EX_DESC = "별명 JSON으로 전달";
    public static final String CHECK_NICKNAME_EX_VAL = """
            {
                "nickname":"별명"
            }
            """;

    // 아이디 중복 체크
    public static final String MEMBER_ID_DESC = "아이디 중복 체크 API";
    public static final String MEMBER_ID_SUCCESS_DESC = "성공시 200코드 반환";
    public static final String MEMBER_ID_FAILED_DESC = "실패시 500코드 반환";
    public static final String MEMBER_ID_EX_DESC = "아이디 JSON으로 전달";
    public static final String MEMBER_ID_EX_VAL = """
            {
                "memberId":"아이디"
            }
            """;
}