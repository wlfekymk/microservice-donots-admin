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
@Table(name = "ADMIN_SYSTEM_ACCESS_LOG")
public class AdminSystemAccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "ADMIN_ID")
    private String adminId;

    @Column(name = "ADMIN_USER_NAME")
    private String adminUserName;

    @Column(name = "ADMIN_USER_NUMBER")
    private String adminUserNumber;

    @Column(name = "ACCESS_DATE")
    private LocalDateTime accessDate;

    @Column(name = "LOGIN_FLAG")
    private Boolean loginFlag;

}
