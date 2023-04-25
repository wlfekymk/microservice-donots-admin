package com.kyobo.platform.donots.model.repository;

import com.kyobo.platform.donots.model.entity.OneOnOneInquiryPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OneOnOneInquiryPostRepository extends JpaRepository<OneOnOneInquiryPost, Long> {

//    Page<OneOnOneInquiryPost> findOrderByInquiredDatetimeDesc(Pageable pageable);
    Page<OneOnOneInquiryPost> findAll(Pageable pageable);
}
