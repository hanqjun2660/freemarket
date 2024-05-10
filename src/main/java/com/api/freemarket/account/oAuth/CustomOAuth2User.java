package com.api.freemarket.account.oAuth;

import com.api.freemarket.account.model.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@RequiredArgsConstructor
public class CustomOAuth2User implements OAuth2User {

    private final UserDTO userDTO;

    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> GrantedRole = new ArrayList<>();

        GrantedRole.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return userDTO.getRole();
            }
        });

        return GrantedRole;
    }

    @Override
    public String getName() {
        return null;
    }

    public Long getMemberNo() {
        return userDTO.getMemberNo();
    }
}
