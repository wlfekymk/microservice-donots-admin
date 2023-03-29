package com.kyobo.platform.donots.oneononeinquiry.service;

import com.kyobo.platform.donots.common.exception.DataNotFoundException;
import com.kyobo.platform.donots.common.exception.ParentNotFoundException;
import com.kyobo.platform.donots.model.entity.service.parent.Parent;
import com.kyobo.platform.donots.model.repository.service.parent.ParentRepository;
import com.kyobo.platform.donots.oneononeinquiry.dto.request.AnswerRequest;
import com.kyobo.platform.donots.oneononeinquiry.dto.response.OneOnOneInquiryPostDetailsListResponse;
import com.kyobo.platform.donots.oneononeinquiry.dto.response.OneOnOneInquiryPostDetailsResponse;
import com.kyobo.platform.donots.oneononeinquiry.entity.OneOnOneInquiryPost;
import com.kyobo.platform.donots.oneononeinquiry.repository.OneOnOneInquiryPostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OneOnOneInquiryService {

    private final OneOnOneInquiryPostRepository oneOnOneInquiryPostRepository;
    private final ParentRepository parentRepository;

    @Transactional
    public void answer(Long oneOnOneInquiryPostKey, AnswerRequest answerRequest, String adminId) throws DecoderException, IOException {

        OneOnOneInquiryPost foundOneOnOneInquiryPost = oneOnOneInquiryPostRepository.findById(oneOnOneInquiryPostKey)
                                                                                    .orElseThrow(() -> new DataNotFoundException());

        // TODO null 체크 해서 업데이트 해야하나?
        foundOneOnOneInquiryPost.setCategory(answerRequest.getCategory());
        foundOneOnOneInquiryPost.setAnswer(answerRequest.getAnswer());

        // Request에서 요청한 상태(Status)값이 기존과 달라졌다면
        if (answerRequest.getStatus() != foundOneOnOneInquiryPost.getStatus()) {
            foundOneOnOneInquiryPost.setStatus(answerRequest.getStatus());

            if (answerRequest.getStatus() == OneOnOneInquiryPost.Status.ANSWER_COMPLETED) {
                foundOneOnOneInquiryPost.setAdminId(adminId);
                foundOneOnOneInquiryPost.setAnswerCompletedDatetime(LocalDateTime.now());
            }
            else if (answerRequest.getStatus() == OneOnOneInquiryPost.Status.ANSWER_PENDING) {
                foundOneOnOneInquiryPost.setAdminId(null);
                foundOneOnOneInquiryPost.setAnswerCompletedDatetime(null);
            }
        }
    }

    public void deleteInquiryPost(Long oneOnOneInquiryPostKey) {

        // FIXME 이미지 삭제
        oneOnOneInquiryPostRepository.deleteById(oneOnOneInquiryPostKey);
    }

    public OneOnOneInquiryPostDetailsListResponse findOneOnOneInquiryPostDetailsListByParentKey(Pageable pageable) {

//        Page<OneOnOneInquiryPost> foundOneOnOneInquiryPostPage = oneOnOneInquiryPostRepository.findOrderByInquiredDatetimeDesc(pageable);
        Page<OneOnOneInquiryPost> foundOneOnOneInquiryPostPage = oneOnOneInquiryPostRepository.findAll(pageable);

        List<OneOnOneInquiryPostDetailsResponse> oneOnOneInquiryPostDetailsResponseList = foundOneOnOneInquiryPostPage.getContent().stream()
                .map(m -> new OneOnOneInquiryPostDetailsResponse(
                        m,
                        parentRepository.findById(m.getParentKey()).orElseThrow(() -> new ParentNotFoundException()).getEmail())
                )
                .collect(Collectors.toList());

        return new OneOnOneInquiryPostDetailsListResponse(oneOnOneInquiryPostDetailsResponseList, foundOneOnOneInquiryPostPage.getTotalPages(), foundOneOnOneInquiryPostPage.getTotalElements());
    }

    public OneOnOneInquiryPostDetailsResponse findOneOnOneInquiryPostDetailsByKey(Long oneOnOneInquiryPostKey) {

        OneOnOneInquiryPost foundOneOnOneInquiryPost = oneOnOneInquiryPostRepository.findById(oneOnOneInquiryPostKey)
                                                                                    .orElseThrow(() -> new DataNotFoundException());

        Parent foundParent = parentRepository.findById(foundOneOnOneInquiryPost.getParentKey())
                                             .orElseThrow(() -> new DataNotFoundException());

        return new OneOnOneInquiryPostDetailsResponse(foundOneOnOneInquiryPost, foundParent.getEmail());
    }
}
