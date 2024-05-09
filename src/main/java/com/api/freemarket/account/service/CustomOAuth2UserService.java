package com.api.freemarket.account.service;

import com.api.freemarket.account.oAuth.GoogleResponse;
import com.api.freemarket.account.oAuth.KakaoResponse;
import com.api.freemarket.account.oAuth.NaverResponse;
import com.api.freemarket.account.oAuth.OAuth2Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registration = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response oAuth2Response = null;

        if(registration.equals("kakao")) {
            log.info("Kakao oAuth Client");
            oAuth2Response = new KakaoResponse(oAuth2User.getAttributes());
        } else if(registration.equals("naver")) {
            log.info("Naver oAuth Client");
            oAuth2Response = new NaverResponse(oAuth2User.getAttributes());
        } else if(registration.equals("google")) {
            log.info("Google oAuth Client");
            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
        } else {
            return null;        // 처리좀 고민해봐야 할듯
        }

        return null;
    }
}
