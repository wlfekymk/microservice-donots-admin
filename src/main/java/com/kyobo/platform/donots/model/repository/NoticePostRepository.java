package com.kyobo.platform.donots.model.repository;

import com.kyobo.platform.donots.model.entity.NoticePost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoticePostRepository extends JpaRepository<NoticePost, Long> {

    NoticePost findByNoticePostKey(Long noticePostKey);
    Page<NoticePost> findByOrderByCreatedDateDesc(Pageable pageable);
    Page<NoticePost> findByTitleContainingOrBodyContainingOrderByCreatedDateDesc(String searchTitle, String searchBody, Pageable pageable);
}
