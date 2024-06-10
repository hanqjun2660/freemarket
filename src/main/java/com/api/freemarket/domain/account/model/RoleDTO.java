package com.api.freemarket.domain.account.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Schema(description = "권한 정보 조회, 권한 추가 및 수정용 DTO")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RoleDTO {

    private Long roleNo;

    private String name;

    private Long memberNo;
}
