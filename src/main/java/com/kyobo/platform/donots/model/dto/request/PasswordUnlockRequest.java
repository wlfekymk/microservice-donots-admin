package com.kyobo.platform.donots.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Getter
public class PasswordUnlockRequest {

    public PasswordUnlockRequest(){};


    @NotBlank
    @Schema(description = "아이디")
    private String adminId;

    @NotBlank
    @Schema(description = "작업 관리자 비밀번호")
    private String regeditAdminPassword;

}
