package com.api.freemarket.domain.account.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.*;

@Schema(description = "회원가입 시 user정보 + address정보 DTO")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserAndAddressDTO {

    @Valid
    private UserDTO userDTO;

    @Valid
    private AddressDTO addressDTO;

}
