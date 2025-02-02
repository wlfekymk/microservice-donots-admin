package com.kyobo.platform.donots.controller;

import com.kyobo.platform.donots.model.dto.request.AdminUserSearchType;
import com.kyobo.platform.donots.model.dto.request.CreateAdminUserRequest;
import com.kyobo.platform.donots.model.dto.request.ModifyAdminUserRequest;
import com.kyobo.platform.donots.model.dto.request.PasswordUnlockRequest;
import com.kyobo.platform.donots.model.dto.response.AdminUserDetailResponse;
import com.kyobo.platform.donots.model.dto.response.AdminUserListResponse;
import com.kyobo.platform.donots.model.dto.response.AdminUserResponse;
import com.kyobo.platform.donots.service.LoginService;
import com.kyobo.platform.donots.service.SuperAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Map;

@Slf4j
@RequestMapping("/super/admin")
@RequiredArgsConstructor
@RestController
public class SuperAdminController {

    private final SuperAdminService superAdminService;

    @PostMapping("/v1/admin-user")
    @Operation(summary = "관리자 생성", description = "")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = AdminUserResponse.class))),
            @ApiResponse(responseCode = "1000", description = "이미 가입된 아이디입니다."),
            @ApiResponse(responseCode = "4000", description = "파라메터 인자값이 정상적이지 않습니다.")
    })
    public ResponseEntity createAdminUser(@RequestBody @Valid CreateAdminUserRequest createAdminUserRequest, HttpServletRequest httpServletRequest) {

        AdminUserResponse adminUserResponse = superAdminService.createAdminUser(createAdminUserRequest, httpServletRequest);
        return new ResponseEntity(adminUserResponse, HttpStatus.CREATED);
    }

    @DeleteMapping("/v1/admin-user/{id}")
    @Operation(summary = "관리자 ID 삭제", description = "관리자 ID 삭제")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "4000", description = "파라메터 인자값이 정상적이지 않습니다.")
    })
    public ResponseEntity deleteAdminUser(@PathVariable("id") Long id, HttpServletRequest httpServletRequest) {
        superAdminService.deleteAdminUser(id, httpServletRequest);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/v1/admin-user")
    @Operation(summary = "관리자 ID 정보 변경  ", description = "관리자 정보 변경")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "4000", description = "파라메터 인자값이 정상적이지 않습니다.")
    })
    public ResponseEntity modifyAdminUser(@RequestBody @Valid ModifyAdminUserRequest modifyAdminUserRequest, HttpServletRequest httpServletRequest) {
        superAdminService.modifyAdminUser(modifyAdminUserRequest, httpServletRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/v1/admin-user/{id}")
    @Operation(summary = "관리자 상세조회", description = "")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = AdminUserDetailResponse.class))),
            @ApiResponse(responseCode = "1000", description = "이미 가입된 아이디입니다."),
            @ApiResponse(responseCode = "4000", description = "파라메터 인자값이 정상적이지 않습니다.")
    })
    public ResponseEntity getAdminUser(@PathVariable("id") Long id, HttpServletRequest httpServletRequest) {
        AdminUserDetailResponse userDetails = superAdminService.loadUserById(id, httpServletRequest);
        return new ResponseEntity(userDetails, HttpStatus.OK);
    }

    @GetMapping("/v1/admin-user")
    @Operation(summary = "관리자 전체조회", description = "")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = AdminUserListResponse.class))),
            @ApiResponse(responseCode = "1000", description = "이미 가입된 아이디입니다."),
            @ApiResponse(responseCode = "4000", description = "파라메터 인자값이 정상적이지 않습니다.")
    })
    public ResponseEntity getAdminUserList(@RequestParam(required = false) final String search, final AdminUserSearchType type, final Pageable pageable, HttpServletRequest httpServletRequest) {
        AdminUserListResponse response = superAdminService.getAdminUserAll(search, pageable, type, httpServletRequest);
        return new ResponseEntity(response, HttpStatus.OK);
    }

    @GetMapping("/v1/verification/{adminId}")
    @Operation(summary = "관리자 ID 가입확인", description = "관리자 ID 중복확인")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "4000", description = "파라메터 인자값이 정상적이지 않습니다.")
    })
    public ResponseEntity idVerification(@PathVariable("adminId") String adminId) {
        Map<String, Boolean> result = superAdminService.verification(adminId);
        return new ResponseEntity(result, HttpStatus.OK);
    }

    @PostMapping("/v1/password/unlock")
    @Operation(summary = "관리자 ID 정보 변경  ", description = "관리자 패스워드 잠금 해제")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "4000", description = "파라메터 인자값이 정상적이지 않습니다.")
    })
    public ResponseEntity passwordUnlock(@RequestBody @Valid PasswordUnlockRequest passwordUnlockRequest, HttpServletRequest httpServletRequest) {
        superAdminService.passwordUnlock(passwordUnlockRequest, httpServletRequest);
        return ResponseEntity.ok().build();
    }

}
