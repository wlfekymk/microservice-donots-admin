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
@Table(name = "ADMIN_ACCESS_PERMISSION")
public class AdminAccessPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "PERMISSION_CATEGORY")
    private PermissionCategory permissionCategory;

    @Column(name = "ADMIN_ID")
    private String adminId;

    @Column(name = "REGEDIT_ADMIN_ID")
    private String regeditAdminId;

    @Column(name = "CREATE_DATE")
    private LocalDateTime createdDate;

}
