package com.kyobo.platform.donots.model.dto.request;

public enum ResetType {
    CHANGE("기간변경"),
    LOCK("틀림잠김");

    public String type;

    ResetType(String type) {
        this.type = type;
    }
}
