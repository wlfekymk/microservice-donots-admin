package com.kyobo.platform.donots.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "PUSH")
public class Push {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "REGEDIT_ADMIN_ID")
    private String regeditAdminId;

    @Column(name = "CONTENT_BODY")
    private String contentBody;

    @Column(name = "CONTENT_LINK_URL")
    private String contentLinkUrl;

    @Column(name = "IMAGE_URL")
    private String imageUrl;

    @Column(name = "ATTACH_FILE_NAME")
    private String attachFileName;

    @Column(name = "REGEDIT_DATE")
    private LocalDateTime regeditDate;

    @Column(name = "RESERVATION_DATE")
    private LocalDateTime reservationDate;

    @Column(name = "IS_SEND_FLAG")
    private Boolean isSendFlag;

    public void updateImageInfo(String attachFileName, String imageUrl){
        this.imageUrl = imageUrl;
        this.attachFileName = attachFileName;
    }

}
