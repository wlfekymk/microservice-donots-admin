package com.kyobo.platform.donots.controller;

import com.kyobo.platform.donots.model.dto.request.NoticeRequest;
import com.kyobo.platform.donots.model.dto.response.NoticeListResponse;
import com.kyobo.platform.donots.model.dto.response.NoticeResponse;
import com.kyobo.platform.donots.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/noAuth")
@RequiredArgsConstructor
@Slf4j
public class NoticeController {

    private final NoticeService noticeService;

    @PostMapping("/v1/notice/post/regedit")
    @Operation(summary = "공지사항 등록", description = "관리자 공지사항 게시판 등록")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "성공"),
            @ApiResponse(responseCode = "403", description = "권한이 없습니다."),
            @ApiResponse(responseCode = "500", description = "실패")
    })
    public ResponseEntity noticeRegedit (@RequestBody @Valid NoticeRequest noticeRequest) {
        Long result = noticeService.noticeRegedit(noticeRequest);
        return new ResponseEntity(result, HttpStatus.CREATED);
    }
    @Operation(summary = "공지사항 삭제", description = "관리자 공지사항 게시판 삭제")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "권한이 없습니다."),
            @ApiResponse(responseCode = "500", description = "실패")
    })
    public ResponseEntity deleteNotice(@PathVariable("noticePostKey") Long noticePostKey) {
        noticeService.deleteNotice(noticePostKey);
        return new ResponseEntity("ok", HttpStatus.OK);
    }
    @PutMapping("/v1/notice/post/updateNotice/{noticePostKey}")
    @Operation(summary = "공지사항 수정", description = "관리자 공지사항 게시판 수정")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = NoticeResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한이 없습니다."),
            @ApiResponse(responseCode = "500", description = "실패")
    })
    public ResponseEntity updateNotice(@PathVariable("noticePostKey") Long noticePostKey, @RequestBody @Valid NoticeRequest noticeRequest) {
        NoticeResponse result = noticeService.updateNotice(noticePostKey, noticeRequest);
        return new ResponseEntity(result, HttpStatus.OK);
    }

    @GetMapping("/v1/notice/post/getNoticeDetail/{noticePostKey}")
    @Operation(summary = "공지사항 상세 조회", description = "유저/관리자 공지사항 게시판 등록")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = NoticeResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한이 없습니다."),
            @ApiResponse(responseCode = "500", description = "실패")
    })
    public ResponseEntity getNoticeDetail (@PathVariable("noticePostKey") Long noticePostKey) {
        NoticeResponse result = noticeService.getNoticeDetail(noticePostKey);
        return new ResponseEntity(result, HttpStatus.OK);
    }

    @GetMapping("/v1/notice/post/getNoticeList")
    @Operation(summary = "공지사항 리스트 조회", description = "유저/관리자 공지사항 게시판 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = NoticeResponse.class))),
    })
    public ResponseEntity getNoticeList () {
        return new ResponseEntity(new NoticeListResponse(noticeService.getNoticeList()), HttpStatus.OK);
    }
}
