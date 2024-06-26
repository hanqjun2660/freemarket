package com.api.freemarket.domain.account.service;

import com.api.freemarket.domain.account.entity.Role;
import com.api.freemarket.domain.account.entity.User;
import com.api.freemarket.domain.account.enums.RoleName;
import com.api.freemarket.domain.account.model.PrincipalDetails;
import com.api.freemarket.domain.account.model.UserDTO;
import com.api.freemarket.domain.account.oAuth.GoogleResponse;
import com.api.freemarket.domain.account.oAuth.KakaoResponse;
import com.api.freemarket.domain.account.oAuth.OAuth2Response;
import com.api.freemarket.domain.account.repository.RoleRepository;
import com.api.freemarket.domain.account.repository.UserRepository;
import com.api.freemarket.domain.account.oAuth.NaverResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final ModelMapper modelMapper;

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registration = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response oAuth2Response = null;

        if("kakao".equals(registration)) {
            log.info("Kakao oAuth Client");
            oAuth2Response = new KakaoResponse(oAuth2User.getAttributes());
        } else if("naver".equals(registration)) {
            log.info("Naver oAuth Client");
            oAuth2Response = new NaverResponse(oAuth2User.getAttributes());
        } else if("google".equals(registration)) {
            log.info("Google oAuth Client");
            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
        } else {
            return null;        // 처리좀 고민해봐야 할듯
        }

        String memberId = oAuth2Response.getProvider() + "_" + oAuth2Response.getProviderId();

        Optional<User> existUser = Optional.ofNullable(userRepository.findByMemberId(memberId));

        if(!existUser.isPresent()) {
           /* User registUser = User.builder()
                    .name(oAuth2Response.getName())
                    .email(oAuth2Response.getEmail())
                    .nickname(oAuth2Response.getName())
                    .provider(oAuth2Response.getProvider())
                    .memberId(memberId)
                    .profileImg(oAuth2Response.getProfileImage())
                    .build();

            User findUser = userRepository.save(registUser);
            Role insertRole = Role.builder()
                    .memberNo(findUser.getMemberNo())
                    .name(RoleName.ROLE_USER.toString())
                    .build();
            Role role = roleRepository.save(insertRole);
            UserDTO userDTO = modelMapper.map(findUser, UserDTO.class);
            userDTO.setRole(role.getName());*/

            UserDTO userDTO = new UserDTO();
            userDTO.setMemberId(memberId);
            userDTO.setName(oAuth2Response.getName());
            userDTO.setEmail(oAuth2Response.getEmail());
            userDTO.setProfileImg(oAuth2Response.getProfileImage());
            userDTO.setProvider(oAuth2Response.getProvider());

            return new PrincipalDetails(userDTO, oAuth2User.getAttributes());
        } else {
            Role role = roleRepository.findByMemberNo(existUser.get().getMemberNo());
            User user = existUser.get();

            UserDTO userDTO = modelMapper.map(user, UserDTO.class);
            userDTO.setRole(role.getName());

            return new PrincipalDetails(userDTO, oAuth2User.getAttributes());
        }
    }
}
