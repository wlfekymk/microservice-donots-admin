package com.kyobo.platform.donots.model.entity;

public enum PermissionCategory {

    C("생성"),
    R("읽기"),
    U("변경"),
    D("삭제");

    public String name;
    PermissionCategory(String name) {
        this.name = name;
    }
}
