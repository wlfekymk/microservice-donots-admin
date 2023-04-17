package com.kyobo.platform.donots.model.repository;

import com.kyobo.platform.donots.model.entity.AdminSystemAccessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminSystemAccessLogRepository  extends JpaRepository<AdminSystemAccessLog, Long> {
}
