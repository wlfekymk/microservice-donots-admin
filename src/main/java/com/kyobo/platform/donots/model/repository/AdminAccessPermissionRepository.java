package com.kyobo.platform.donots.model.repository;

import com.kyobo.platform.donots.model.entity.AdminAccessPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminAccessPermissionRepository extends JpaRepository<AdminAccessPermission, Long> {
}
