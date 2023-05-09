package com.kyobo.platform.donots.model.dto.request;

public enum PushSearchType {
    ALL("전체"),
    TITLE("제목"),
    REGEDIT("등록자"),
    FILENAME("첨부파일명");

    public String name;

    PushSearchType(String name){
        this.name = name;
    }
}
