package com.api.freemarket.account.oAuth;

import java.util.Map;

public class KakaoResponse implements OAuth2Response {

    private final Map<String, Object> properties;

    private final Map<String, Object> account;

    private final String providerId;

    public KakaoResponse(Map<String, Object> attributes) {
        this.properties = (Map<String, Object>) attributes.get("properties");
        this.account = (Map<String, Object>) attributes.get("kakao_account");
        this.providerId = attributes.get("id").toString();
    }

    @Override
    public String getProvider() {
        return null;
    }

    @Override
    public String getProviderId() {
        return null;
    }

    @Override
    public String getEmail() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getProfileImage() {
        return null;
    }
}
