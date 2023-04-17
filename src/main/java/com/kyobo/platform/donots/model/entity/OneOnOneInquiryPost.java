package com.kyobo.platform.donots.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "one_on_one_inquiry_post")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class OneOnOneInquiryPost {

    @Id
    @GeneratedValue
    @Column(name = "one_on_one_inquiry_post_key")
    private Long key;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Column(nullable = false)
    private String inquiryTitle;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String inquiryBody;
    private String attachmentFileUrl;

    private Long parentKey;
    private LocalDateTime inquiredDatetime;

    @Column(columnDefinition = "TEXT")
    private String answer;
    private String adminId;
    private LocalDateTime answerCompletedDatetime;

    @Enumerated(EnumType.STRING)
    private Status status;

    public enum Category {

        GENERAL("일반"),
        SIGN_UP_AND_SIGN_IN("가입/로그인"),
        RECIPE_POST("레시피"),
        BABY_PROFILE("아이 프로필"),
        MEMBER_RANKING_BOARD("랭킹"),
        MISCELLANY("기타");

        public String name;

        Category(String name) {
            this.name = name;
        }
    }

    public enum Status {
        ANSWER_PENDING, ANSWER_COMPLETED
    }
}
