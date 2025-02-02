package com.kyobo.platform.donots.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kyobo.platform.donots.model.entity.AdminUser;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AdminUserDetailResponse {

    public AdminUserDetailResponse() {
    }

    @Schema(description = "어드민 고유 번호")
    private Long id;

    @Schema(description = "어드민 id")
    private String adminId;

    @Schema(description = "어드민 유저 이름")
    private String adminUserName;

    @Schema(description = "어드민 유저 사번")
    private String adminUserNumber;

    @Schema(description = "부서명")
    private String departmentName;

    @Schema(description = "Role : SUPER_ADMIN, ADMIN")
    private String role;

    @Schema(description = "폰번호")
    private String phoneNumber;

    @Schema(description = "이메일")
    private String email;

    @Schema(description = "메모")
    private String memo;

    @Schema(description = "첨부파일")
    private String attachImageUrl;

    @Schema(description = "등록해준 어드민 ID")
    private String regeditAdminId;

    @Schema(description = "사유")
    private String reasonsForAuthorization;

    @Schema(description = "헤더정보")
    private String headerInfo;

    @Schema(description = "3개월 패스워드 변경 flag")
    private Boolean isThreeMonthPasswordChangeFlag;

    @Schema(description = "계정잠김 변경 flag")
    private Boolean isAccountLockPasswordChangeFlag;
    @Schema(description = "마지막 접속 일")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastSignInDate;

    @Schema(description = "마지막 패스워드 변경일")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastPasswordChangeDate;

    @Schema(description = "계정 생성일")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdDate;

    public AdminUserDetailResponse(AdminUser adminUser) {
        this.adminId = adminUser.getAdminId();
        this.adminUserName = adminUser.getAdminUserName();
        this.adminUserNumber = adminUser.getAdminUserNumber();
        this.id = adminUser.getId();
        this.lastPasswordChangeDate = adminUser.getLastPasswordChangeDate();
        this.attachImageUrl = adminUser.getAttachImageUrl();
        this.departmentName = adminUser.getDepartmentName();
        this.email = adminUser.getEmail();
        this.lastSignInDate = adminUser.getLastSignInDate();
        this.memo = adminUser.getMemo();
        this.phoneNumber = adminUser.getPhoneNumber();
        this.reasonsForAuthorization = adminUser.getReasonsForAuthorization();
        this.role = adminUser.getRole();
        this.regeditAdminId = adminUser.getRegeditAdminId();
        this.createdDate = adminUser.getCreatedDate();
        this.headerInfo = adminUser.getSessionId();

        if (adminUser.getLoginCount() == 0)
            this.isThreeMonthPasswordChangeFlag = true;
        else if (adminUser.getLastPasswordChangeDate().plusMonths(3).isBefore(LocalDateTime.now()))
            this.isThreeMonthPasswordChangeFlag = true;
        else
            this.isThreeMonthPasswordChangeFlag = false;

        this.isAccountLockPasswordChangeFlag = adminUser.isAccountLockPasswordChangeFlag();
    }
}
