package com.kyobo.platform.donots.oneononeinquiry.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kyobo.platform.donots.oneononeinquiry.entity.OneOnOneInquiryPost;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.time.LocalDateTime;


@Getter
public class OneOnOneInquiryPostDetailsResponse {

    @Schema(description = "1:1문의 게시글 키")
    private Long key;

    @Schema(description = "문의제목")
    private String inquiryTitle;

    @Schema(description = "문의본문")
    private String inquiryBody;

    @Schema(description = "답변")
    private String answer;

    @Schema(description = "첨부파일 URL")
    private String attachmentFileUrl;

    @Schema(description = "Email")
    private String email;

    @Schema(description = "회원 키")
    private Long parentKey;

    @Schema(description = "등록한 관리자ID")
    private String adminId;

    @Schema(description = "문의유형")
    @Enumerated(EnumType.STRING)
    private OneOnOneInquiryPost.Category category;

    @Schema(description = "문의처리상태")
    @Enumerated(EnumType.STRING)
    private OneOnOneInquiryPost.Status status;

    @Schema(description = "문의일시")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime inquiredDatetime;

    @Schema(description = "답변완료일시")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime answerCompletedDatetime;

    public OneOnOneInquiryPostDetailsResponse(OneOnOneInquiryPost oneOnOneInquiryPost, String email) {
        this.key = oneOnOneInquiryPost.getKey();
        this.inquiryTitle = oneOnOneInquiryPost.getInquiryTitle();
        this.inquiryBody = oneOnOneInquiryPost.getInquiryBody();
        this.answer = oneOnOneInquiryPost.getAnswer();
        this.attachmentFileUrl = oneOnOneInquiryPost.getAttachmentFileUrl();
        this.email = email;
        this.parentKey = oneOnOneInquiryPost.getParentKey();
        this.adminId = oneOnOneInquiryPost.getAdminId();
        this.category = oneOnOneInquiryPost.getCategory();
        this.status = oneOnOneInquiryPost.getStatus();
        this.inquiredDatetime = oneOnOneInquiryPost.getInquiredDatetime();
        this.answerCompletedDatetime = oneOnOneInquiryPost.getAnswerCompletedDatetime();
    }
}
