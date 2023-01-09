package com.kyobo.platform.donots.controller;

import com.kyobo.platform.donots.model.dto.request.ChangePasswordRequest;
import com.kyobo.platform.donots.model.dto.request.CreateAdminUserRequest;
import com.kyobo.platform.donots.model.dto.request.SignInRequest;
import com.kyobo.platform.donots.service.LoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@Slf4j
@RequestMapping("/login")
@RequiredArgsConstructor
@RestController
public class LoginController {
    private final LoginService loginService;

    @PostMapping("/change/password")
    @Operation(summary = "패스워드 변경 ", description = "")
    @Parameter(name = "adminId", description = "아이디")
    @Parameter(name = "password", description = "현재 비밀번호")
    @Parameter(name = "newPassword", description = "새 비밀번호")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공")
    })
    public ResponseEntity changePassword(@RequestBody @Valid ChangePasswordRequest changePasswordRequest) {
        Map<String, Boolean> result = loginService.changePasswordRequest(changePasswordRequest);
        return new ResponseEntity(result, HttpStatus.OK);
    }

    @PostMapping("/signIn")
    @Operation(summary = "로그인 ", description = "")
    @Parameter(name = "adminId", description = "아이디")
    @Parameter(name = "password", description = "현재 비밀번호")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공")
    })
    public ResponseEntity signIn(@RequestBody @Valid SignInRequest signInRequest) {
        UserDetails result = loginService.signIn(signInRequest);
        return new ResponseEntity(result, HttpStatus.OK);
    }
}