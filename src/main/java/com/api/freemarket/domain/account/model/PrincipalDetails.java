package com.api.freemarket.domain.account.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class PrincipalDetails implements OAuth2User, UserDetails {

    private UserDTO userDTO;
    
    private Map<String, Object> attributes;
    
    // 일반 로그인용 생성자
    public PrincipalDetails(UserDTO userDTO) {
        this.userDTO = userDTO;
    }

    // 소셜 로그인용 생성자
    public PrincipalDetails(UserDTO userDTO, Map<String, Object> attributes) {
        this.userDTO = userDTO;
        this.attributes = attributes;
    }

    @Override
    public String getName() {
        return userDTO.getName();
    }

    public String getEmail() {
        return userDTO.getEmail();
    }

    public String getMemberId() {
        return userDTO.getMemberId();
    }

    public String getProfileImage() {
        return userDTO.getProfileImg();
    }

    public Long getMemberNo() {
        return userDTO.getMemberNo();
    }

    public String getUserStatus() {
        return userDTO.getStatus();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> grantedRole = new ArrayList<>();

        grantedRole.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return userDTO.getRole();
            }
        });

        return grantedRole;
    }

    @Override
    public String getPassword() {
        return userDTO.getPassword();
    }

    @Override
    public String getUsername() {
        return userDTO.getNickname();
    }

    // 자격증명 부분 구현해야함 지금은 다 true라 됨
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}