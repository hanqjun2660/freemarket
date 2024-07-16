package com.api.freemarket.config.swagger;

public final class SwaggerAccountDesc {

    // 일반 회원 로그인
    public static final String NORMAL_USER_LOGIN_DESC = """
        일반 회원의 로그인 API
        - 아이디, 패스워드 JSON으로 전달
        """;
    public static final String NORMAL_USER_LOGIN_SUCCESS_DESC = "성공시 200코드 반환";
    public static final String NORMAL_USER_LOGIN_FAILED_DESC = "실패시 500코드 반환";
    public static final String NORMAL_USER_LOGIN_EX_VAL = """
            {
                "memberId":"회원 아이디",
                "password":"비밀번호"
            }
            """;

    public static final String NORMAL_USER_LOGIN_SUCCESS_EX_VAL = """
       {
            "로그인 시" : {
                "statusCode": "200",
                "httpStatus": null,
                "message": null,
                "data": null
            },
            
            "임시 비밀번호 발급 회원 로그인 시" : {
                "statusCode": "200",
                "httpStatus": null,
                "message": "임시 비밀번호를 발급 받았습니다. 비밀번호 변경 페이지로 이동합니다.",
                "data": {
                    "tempPassStatus" : "Y"
                }
            }
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
    public static final String JOIN_DESC = """
        회원가입 API
        - 아이디,비밀번호,이름,별명,이메일,(주소: 시,도 / 시,군,구 / 동,읍,면) JSON으로 전달
        """;
    public static final String JOIN_SUCCESS_DESC = "성공시 200코드 반환";
    public static final String JOIN_FAILED_DESC = "실패시 500코드 반환";
    public static final String JOIN_EX_VAL = """
            {
                "userDTO": {
                    "memberId": "아이디",
                    "password": "비밀번호",
                    "name": "이름",
                    "nickname": "닉네임",
                    "phone": "01012345678",
                    "email": "abcd@gmail.com"
                },
                "addressDTO": {
                    "address1": "서울시",
                    "address2": "강남구",
                    "address3": "도곡동"
                }
            }
            """;

    // 별명 중복 체크
    public static final String CHECK_NICKNAME_DESC = """
    별명 중복 체크 API
    - 별명 JSON으로 전달
    """;
    public static final String CHECK_NICKNAME_SUCCESS_DESC = "성공시 200코드 반환";
    public static final String CHECK_NICKNAME_FAILED_DESC = "실패시 500코드 반환";
    public static final String CHECK_NICKNAME_EX_VAL = """
            {
                "nickname":"별명"
            }
            """;

    // 아이디 중복 체크
    public static final String MEMBER_ID_DESC = """
    아이디 중복 체크 API
    - 아이디 JSON으로 전달
    """;
    public static final String MEMBER_ID_SUCCESS_DESC = "성공시 200코드 반환";
    public static final String MEMBER_ID_FAILED_DESC = "실패시 500코드 반환";
    public static final String MEMBER_ID_EX_VAL = """
            {
                "memberId":"아이디"
            }
            """;

    // 소셜 로그인 유저 추가 정보
    public static final String SOCIAL_USER_JOIN_DESC = """
    소셜 로그인 유저 회원가입 시 추가정보 저장 API
    - 소셜로그인 성공 시 쿠키에 email 값을 넣어줌(화면 input에 고정으로 입력 후 해당 값도 같이 전달) 
    - userDTO (이메일, 닉네임)
    """;
    public static final String SOCIAL_USER_JOIN_SUCCESS_DESC = "성공시 200코드 반환";
    public static final String SOCIAL_USER_JOIN_FAILED_DESC = "실패시 500코드 반환";
    public static final String SOCIAL_USER_JOIN_EX_VAL = """
          {
            "nickname": "testNick1",
            "email": "test@example.com"
          }
          """;

    // 비밀번호 찾기용 이메일 인증번호 발송 요청
    public static final String FIND_PASSWORD_CERT_DESC = """
            비밀번호 찾기를 위해 아이디, 이메일을 입력받아 해당 사용자가 실제 존재하는지 확인 후
            이메일로 인증번호를 발송하는 API
             - 이메일로 발송된 인증번호를 검증하는 API는 '/api/v1/mail/valid-cert-num'을 사용하여야한다.
            """;
    public static final String FIND_PASSWORD_CERT_SUCCESS_DESC = "성공시 200코드 반환";

    public static final String FIND_PASSWORD_CERT_FAILED_DESC = "성공시 500코드 반환";

    public static final String FIND_PASSWORD_CERT_EX_VAL = """
            {
                "memberId":"아이디",
                "email":"asdf@asdf.com"
            }
            """;

    public static final String FIND_PASSWORD_CERT_SUCCESS_EX_VAL = """
            {
                "statusCode": "200",
                "message": "성공메세지",
                "data": {
                    "duration" : 1800000
                }
            }
            """;
    
    // 임시 비밀번호 발급
    public static final String TEMP_PASSWORD_ISSUED_DESC = """
            비밀번호 찾기용 이메일 인증번호 발송 요청 및 검증이 이루어진 후 임시 비밀번호를
            이메일로 발송해주는 API
            """;

    public static final String TEMP_PASSWORD_ISSUED_SUCCESS_DESC = "성공시 200코드 반환";

    public static final String TEMP_PASSWORD_ISSUED_FAILED_DESC = "성공시 500코드 반환";

    public static final String TEMP_PASSWORD_ISSUED_EX_VAL = """
            {
                "memberId":"아이디",
                "email":"asdf@asdf.com",
                "verify":"Y"
            }
            """;


    // 아이디 찾기용 이메일 인증번호 발송 요청
    public static final String FIND_ID_CERT_DESC = """
            아이디 찾기를 위해 이메일을 입력받아 해당 사용자가 실제 존재하는지 확인 후
            이메일로 인증번호를 발송하는 API
             - 이메일로 발송된 인증번호를 검증하는 API는 '/api/v1/mail/valid-cert-num'을 사용하여야한다.
            """;
    public static final String FIND_ID_CERT_SUCCESS_DESC = "성공시 200코드 반환";

    public static final String FIND_ID_CERT_FAILED_DESC = "성공시 500코드 반환";

    public static final String FIND_ID_CERT_EX_VAL = """
            {
                "email":"asdf@asdf.com"
            }
            """;

    public static final String FIND_ID_CERT_SUCCESS_EX_VAL = """
            {
                "statusCode": "200",
                "message": "성공메세지",
                "data": {
                    "duration" : 1800000
                }
            }
            """;

    // 임시 비밀번호 발급
    public static final String FIND_ID_USER_INFO_DESC = """
            아이디 찾기 이메일 인증 완료 후 소셜로그인유저인지 일반로그인 회원인지를 확인해서
            정보를 RETURN 하는 API
            """;

    public static final String FIND_ID_USER_INFO_SUCCESS_DESC = "성공시 200코드 반환";

    public static final String FIND_ID_USER_INFO_FAILED_DESC = "성공시 500코드 반환";

    public static final String FIND_ID_USER_INFO_EX_VAL = """
            {
                "email":"asdf@asdf.com",
                "verify":"Y"
            }
            """;

    public static final String FIND_ID_USER_INFO_SUCCESS_EX_VAL = """
       {
            "소셜 로그인 시" : {
                "statusCode": "200",
                "message": "성공메세지",
                "data": {
                    "provider" : "naver or kakao or google"
                }
            },
            
            "일반 회원가입 시" : {
                "statusCode": "200",
                "message": "성공메세지",
                "data": {
                    "memberId" : "test***45"
                }
            }
       }
       """;

    // 로그아웃
    public static final String LOGOUT_USER_DESC = """
            로그아웃 요청을 처리하기 위한 API
            - 로그아웃 요청시 만료되지 않은 accessToken이 필요함.
            - 인증받은 사용자(로그인된 사용자)만 요청 가능 -> 상단 Authorize 버튼에 accessToken 입력 후 테스트가 가능
            - 로그아웃이 성공적으로 처리되면 Authorize Header 및 Cookie내 refresh의 value는 null로 변경됨.
            """;
    public static final String LOGOUT_USER_SUCCESS_DESC="성공시 200코드 반환";
    public static final String LOGOUT_USER_FAILED_DESC="성공시 500코드 반환";
}