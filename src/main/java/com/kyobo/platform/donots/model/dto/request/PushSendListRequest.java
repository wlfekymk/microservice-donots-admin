package com.kyobo.platform.donots.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class PushSendListRequest {

    public PushSendListRequest(){

    }

    @Schema(description = "검색타입")
    @NotBlank
    @Enumerated(EnumType.STRING)
    private PushSearchType searchType;

    @Schema(description = "검색문자")
    private String searchWord;

    @Schema(description = "검색 시작 시간")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String start;

    @Schema(description = "검색 종료 시간")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String end;

}
