package com.api.freemarket.domain.account.model;

import com.api.freemarket.common.validation.ValidationGroups;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
public class AddressDTO {

    private Long memberNo;
    @NotBlank(message = "필수입력 항목이 비었습니다.(시, 도)", groups = {ValidationGroups.addInfoValidation.class})
    private String address1;
    @NotBlank(message = "필수입력 항목이 비었습니다.(시, 군, 구)", groups = {ValidationGroups.addInfoValidation.class})
    private String address2;
    @NotBlank(message = "필수입력 항목이 비었습니다.(동, 읍, 면)", groups = {ValidationGroups.addInfoValidation.class})
    private String address3;

}
