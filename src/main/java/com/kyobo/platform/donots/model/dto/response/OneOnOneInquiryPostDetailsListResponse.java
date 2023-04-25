package com.kyobo.platform.donots.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;

@Getter
public class OneOnOneInquiryPostDetailsListResponse {

    @Schema(description = "1:1문의 게시물 상세 목록")
    private List<OneOnOneInquiryPostDetailsResponse> oneOnOneInquiryPostDetailsResponseList;

    @Schema(description = "총 페이지")
    private int totalPage;

    @Schema(description = "게시물수")
    private Long totalElements;


    public OneOnOneInquiryPostDetailsListResponse(List<OneOnOneInquiryPostDetailsResponse> oneOnOneInquiryPostDetailsResponseList, int totalPage, Long totalElements) {
        this.oneOnOneInquiryPostDetailsResponseList = oneOnOneInquiryPostDetailsResponseList;
        this.totalElements = totalElements;
        this.totalPage = totalPage;
    }
}
