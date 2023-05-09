package com.kyobo.platform.donots.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;

@Getter
public class PushSendListResponse {
    public PushSendListResponse(){

    }

    @Schema(description = "공지사항 리스트")
    private List<PushSendDetailResponse> pushSendDetailResponseList;

    @Schema(description = "총 페이지")
    private int totalPage;

    @Schema(description = "게시물수")
    private Long totalElements;


    public PushSendListResponse(List<PushSendDetailResponse> list, int totalPage, Long totalElements){
        this.pushSendDetailResponseList = list;
        this.totalPage = totalPage;
        this.totalElements = totalElements;
    }
}
