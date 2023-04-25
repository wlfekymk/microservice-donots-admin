package com.kyobo.platform.donots.model.repository;

import com.kyobo.platform.donots.model.entity.PersonalInfoAccessHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonalInfoAccessHistoryRepository extends JpaRepository<PersonalInfoAccessHistory,Long> {

}
