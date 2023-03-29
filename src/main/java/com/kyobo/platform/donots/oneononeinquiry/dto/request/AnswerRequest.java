package com.kyobo.platform.donots.oneononeinquiry.dto.request;

import com.kyobo.platform.donots.oneononeinquiry.entity.OneOnOneInquiryPost;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotBlank;

@Getter
public class AnswerRequest {

    @Schema(description = "문의유형")
    @Enumerated(EnumType.STRING)
    private OneOnOneInquiryPost.Category category;

    @Schema(description = "답변")
    @NotBlank
    private String answer;

    @Schema(description = "상태")
    @Enumerated(EnumType.STRING)
    private OneOnOneInquiryPost.Status status;
}
