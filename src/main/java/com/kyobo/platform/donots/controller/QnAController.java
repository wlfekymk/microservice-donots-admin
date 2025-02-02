package com.kyobo.platform.donots.controller;


import com.kyobo.platform.donots.common.util.SessionUtil;
import com.kyobo.platform.donots.model.dto.request.QnAListRequest;
import com.kyobo.platform.donots.model.dto.request.QnAUpdateRequest;
import com.kyobo.platform.donots.model.dto.response.QnAListResponse;
import com.kyobo.platform.donots.service.QnAService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;


@Controller
@RequiredArgsConstructor
@RequestMapping("/qna")
@Slf4j
public class QnAController {
    private final QnAService qnAService;

    private final RedisTemplate<String, Object> redisTemplate;

    @PutMapping("/v1/post")
    @Operation(summary = "Q&A 답변 ", description = "")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공")
    })
    public ResponseEntity<?> qnAUpdate(@RequestBody @Valid QnAUpdateRequest qnAUpdateRequest, HttpServletRequest httpServletRequest)  {
        String adminIdFromSession = SessionUtil.getGlobalCustomSessionStringAttribute("adminId", httpServletRequest, redisTemplate);
        qnAService.qnAUpdate(qnAUpdateRequest, adminIdFromSession);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/v1/post")
    @Operation(summary = "Q&A 리스트 ", description = "")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = QnAListResponse.class)))
    })
    public ResponseEntity<?> getQnAList(@ModelAttribute QnAListRequest qnAListRequest, Pageable pageable)  {
        QnAListResponse qnAListResponse = qnAService.getQnAList(qnAListRequest, pageable);
        return new ResponseEntity(qnAListResponse, HttpStatus.OK);
    }
}
