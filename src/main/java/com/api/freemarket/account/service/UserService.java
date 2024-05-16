package com.api.freemarket.account.service;

import com.api.freemarket.account.entity.Role;
import com.api.freemarket.account.entity.User;
import com.api.freemarket.account.enums.RoleName;
import com.api.freemarket.account.model.PrincipalDetails;
import com.api.freemarket.account.model.UserDTO;
import com.api.freemarket.account.repository.RoleRepository;
import com.api.freemarket.account.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {

    private final ModelMapper modelMapper;

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    @Override
    public UserDetails loadUserByUsername(String memberId) throws UsernameNotFoundException {
        Optional<User> existUser = Optional.ofNullable(userRepository.findByMemberId(memberId));

        if(!existUser.isPresent()) {
           throw new UsernameNotFoundException("해당 ID의 사용자가 존재하지 않음");
        }

        UserDTO userDTO = modelMapper.map(existUser.get(), UserDTO.class);
        /*Role role = roleRepository.findByMemberNo(userDTO.getMemberNo());
        userDTO.setRole(role.getName());*/
        userDTO.setRole(String.valueOf(RoleName.ROLE_USER));

        return new PrincipalDetails(userDTO);
    }
}
