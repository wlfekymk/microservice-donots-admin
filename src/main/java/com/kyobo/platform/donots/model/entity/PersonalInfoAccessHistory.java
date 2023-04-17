package com.kyobo.platform.donots.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "PERSONAL_INFO_ACCESS_HISTORY")
public class PersonalInfoAccessHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "OPERATOR_ADMIN_ID")
    private String operatorAdminId;

    @Column(name = "OPERATOR_ADMIN_USER_NAME")
    private String operatorAdminUserName;

    @Column(name = "OPERATOR_ADMIN_USER_NUMBER")
    private String operatorAdminUserNumber;

    @Column(name = "INQUIRED_ACCOUNT_KEY")
    private Long inquiredAccountKey;

    @Column(name = "PROCESSING_CONTENT")
    private String processingContent;

    @Column(name = "PROCESSING_API_NAME")
    private String processingApiName;

    @Column(name = "CREATE_DATE")
    private LocalDateTime createdDate;

}
