package com.kyobo.platform.donots.model.dto.response;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.kyobo.platform.donots.model.entity.Push;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
public class PushSendDetailResponse {
    

    public PushSendDetailResponse(){
    }

    @Schema(description = "NO")
    private Long id;

    @Schema(description = "제목")
    private String title;

    @Schema(description = "등록자")
    private String regeditId;

    @Schema(description = "첨부파일")
    private String imageUrl;

    @Schema(description = "본문내용")
    private String contentBody;

    @Schema(description = "랜딩링크")
    private String contentLinkUrl;

    @Schema(description = "등록일자")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime regeditDate;

    @Schema(description = "첨부파일명")
    private String attachFileName;
    @Schema(description = "발송시간")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reservationDate;

    public PushSendDetailResponse(Push push){
        this.id = push.getId();
        this.title = push.getTitle();
        this.contentBody = push.getContentBody();
        this.contentLinkUrl = push.getContentLinkUrl();
        this.imageUrl = push.getImageUrl();
        this.regeditDate = push.getRegeditDate();
        this.regeditId = push.getRegeditAdminId();
        this.reservationDate = push.getReservationDate();
        this.attachFileName = push.getAttachFileName();
    }

}
