package com.api.freemarket.domain.account.service;

import com.api.freemarket.common.CommonResponse;
import com.api.freemarket.domain.account.entity.Role;
import com.api.freemarket.domain.account.entity.User;
import com.api.freemarket.domain.account.enums.MemberStatus;
import com.api.freemarket.domain.account.enums.RoleName;
import com.api.freemarket.domain.account.model.PrincipalDetails;
import com.api.freemarket.domain.account.model.RoleDTO;
import com.api.freemarket.domain.account.model.UserDTO;
import com.api.freemarket.domain.account.repository.RoleRepository;
import com.api.freemarket.domain.account.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        if(MemberStatus.DISABLE.toString().equals(existUser.get().getStatus())){
            throw new UsernameNotFoundException("탈퇴 계정입니다.");
        }
        if(MemberStatus.SLEEP.toString().equals(existUser.get().getStatus())){
            throw new UsernameNotFoundException("휴면 계정입니다.");
        }

        UserDTO userDTO = modelMapper.map(existUser.get(), UserDTO.class);
        Role role = roleRepository.findByMemberNo(userDTO.getMemberNo());
        userDTO.setRole(role.getName());
        /*userDTO.setRole(String.valueOf(RoleName.ROLE_USER));*/

        return new PrincipalDetails(userDTO);
    }

    /*
    @Transactional
    public User insertByMemberNo(UserDTO userDTO, long memberNo) {
        Optional<User> updateUser = Optional.ofNullable(userRepository.findByMemberNo(memberNo));

        if(!updateUser.isPresent()) {
            log.info("해당 회원이 존재하지 않음");
            return new User();
        }

        updateUser.get().setPhone(userDTO.getPhone());

        return userRepository.save(updateUser.get());
    }
    */

    @Transactional
    public User joinUser(UserDTO userDTO) {
        User saveUser = userRepository.save(modelMapper.map(userDTO, User.class));

        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setName(String.valueOf(RoleName.ROLE_USER));
        roleDTO.setMemberNo(saveUser.getMemberNo());

        roleRepository.save(modelMapper.map(roleDTO, Role.class));

        return saveUser;
    }

    public boolean existsByNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    public boolean existsByMemberId(String memberId) {
        return userRepository.existsByMemberId(memberId);
    }
}
