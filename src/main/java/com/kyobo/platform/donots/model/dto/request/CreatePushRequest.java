package com.kyobo.platform.donots.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Getter
@Setter
public class CreatePushRequest {

    @NotBlank
    @Schema(description = "제목")
    private String title;
    
    @Schema(description = "본문내용")
    private String contentBody;

    @Schema(description = "랜딩링크")
    private String contentLinkUrl;

    @Schema(description = "예약시간")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime reservationDate;
}
