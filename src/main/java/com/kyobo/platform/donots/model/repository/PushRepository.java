package com.kyobo.platform.donots.model.repository;

import com.kyobo.platform.donots.model.entity.Push;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface PushRepository extends JpaRepository<Push, Long> {

    Page<Push> findByRegeditDateBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<Push> findByTitleContaining(String searchWord, Pageable pageable);

    Page<Push> findByRegeditDateBetweenAndTitleContaining(LocalDateTime start, LocalDateTime end, String searchWord, Pageable pageable);

    Page<Push> findByRegeditAdminIdContaining(String searchWord, Pageable pageable);

    Page<Push> findByRegeditDateBetweenAndRegeditAdminIdContaining(LocalDateTime start, LocalDateTime end, String searchWord, Pageable pageable);

    Page<Push> findByAttachFileNameContaining(String searchWord, Pageable pageable);

    Page<Push> findByRegeditDateBetweenAndAttachFileNameContaining(LocalDateTime start, LocalDateTime end, String searchWord, Pageable pageable);


}

