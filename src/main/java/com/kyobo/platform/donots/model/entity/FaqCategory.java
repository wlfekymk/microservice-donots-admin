package com.kyobo.platform.donots.model.entity;

public enum FaqCategory {
    GENERAL("일반"),
    SIGN_UP_AND_SIGN_IN("가입/로그인"),
    RECIPE_POST("레시피"),
    BABY_PROFILE("아이 프로필"),
    MEMBER_RANKING_BOARD("랭킹"),
    MISCELLANY("기타");

    public final String name;

    FaqCategory(String name) {
        this.name = name;
    }
}
