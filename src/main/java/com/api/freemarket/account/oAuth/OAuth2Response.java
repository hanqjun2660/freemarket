package com.api.freemarket.account.oAuth;

public interface OAuth2Response {

    // 정보 제공자
    String getProvider();

    // 제공자 발급 아이디
    String getProviderId();

    // 이메일
    String getEmail();

    // 사용자 이름
    String getName();

    // 프로필 이미지
    String getProfileImage();
}
