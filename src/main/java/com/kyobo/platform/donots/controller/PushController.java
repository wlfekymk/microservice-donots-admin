package com.kyobo.platform.donots.controller;

import com.kyobo.platform.donots.common.util.SessionUtil;
import com.kyobo.platform.donots.model.dto.request.CreatePushRequest;
import com.kyobo.platform.donots.model.dto.request.PushSendListRequest;
import com.kyobo.platform.donots.model.dto.response.AdminUserResponse;
import com.kyobo.platform.donots.model.dto.response.NoticeListResponse;
import com.kyobo.platform.donots.model.dto.response.PushSendListResponse;
import com.kyobo.platform.donots.service.PushService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.codec.DecoderException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.net.URI;

@Controller
@RequiredArgsConstructor
@RequestMapping("/push")
@Slf4j
public class PushController {

    private final PushService pushService;

    private final RedisTemplate<String, Object> redisTemplate;

    @PostMapping("/v1/push/send")
    @Operation(summary = "push 발송 요청 생성", description = "관리자 Push 발송 스케줄 요청")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공")
    })
    public ResponseEntity pushSendRegedit(@Valid CreatePushRequest createPushRequest, MultipartFile multipartFile, HttpServletRequest httpServletRequest) throws DecoderException, IOException {

        String adminId = SessionUtil.getGlobalCustomSessionStringAttribute("adminId", httpServletRequest, redisTemplate);

        Long result = pushService.pushSendRegedit(createPushRequest, adminId, multipartFile);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{key}")
                .buildAndExpand(result)
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @GetMapping("/v1/push/send")
    @Operation(summary = "push 발송 요청 List 보기 ", description = "관리자 Push 발송 요청 내역 보기")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = PushSendListResponse.class)))
    })
    public ResponseEntity pushSendList(@ModelAttribute PushSendListRequest pushSendListRequest, Pageable pageable, HttpServletRequest httpServletRequest) {

        log.info("pushSendList start!!");
        PushSendListResponse result = pushService.pushSendList(pushSendListRequest, pageable);

        return new ResponseEntity(result, HttpStatus.OK);
    }



    @DeleteMapping("/v1/push/send/{id}")
    @Operation(summary = "push 발송 요청 삭제", description = "관리자 Push 발송 요청 삭제")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공")
    })
    public ResponseEntity pushSendDelete(@PathVariable("id") Long id, HttpServletRequest httpServletRequest) {

        String adminId = SessionUtil.getGlobalCustomSessionStringAttribute("adminId", httpServletRequest, redisTemplate);

        pushService.pushSendDelete(id, adminId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/v1/push/send")
    @Operation(summary = "push 발송 요청 수정", description = "관리자 Push 발송 요청 수정")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공")
    })
    public ResponseEntity pushSendUpdate(@Valid CreatePushRequest createPushRequest, MultipartFile multipartFile, HttpServletRequest httpServletRequest) throws DecoderException, IOException {

        String adminId = SessionUtil.getGlobalCustomSessionStringAttribute("adminId", httpServletRequest, redisTemplate);

        Long result = pushService.pushSendRegedit(createPushRequest, adminId, multipartFile);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{key}")
                .buildAndExpand(result)
                .toUri();

        return ResponseEntity.created(location).build();
    }
}
