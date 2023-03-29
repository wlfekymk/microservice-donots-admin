package com.kyobo.platform.donots.oneononeinquiry.controller;


import com.kyobo.platform.donots.common.util.SessionUtil;
import com.kyobo.platform.donots.oneononeinquiry.dto.request.AnswerRequest;
import com.kyobo.platform.donots.oneononeinquiry.dto.response.OneOnOneInquiryPostDetailsListResponse;
import com.kyobo.platform.donots.oneononeinquiry.dto.response.OneOnOneInquiryPostDetailsResponse;
import com.kyobo.platform.donots.oneononeinquiry.service.OneOnOneInquiryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequiredArgsConstructor
@Slf4j
public class OneOnOneInquiryController {

    private final OneOnOneInquiryService oneOnOneInquiryService;

    private final RedisTemplate<String, Object> redisTemplate;

    @PatchMapping("/v1/one-on-one-inquiry-posts/by-admin/{key}")
    @Operation(summary = "1:1문의 > 답변")
    public ResponseEntity<?> answer(@PathVariable("key") Long oneOnOneInquiryPostKey, @RequestBody @Valid AnswerRequest answerRequest, HttpServletRequest httpServletRequest) throws DecoderException, IOException {

        String adminIdFromSession = SessionUtil.getGlobalCustomSessionStringAttribute("adminId", httpServletRequest, redisTemplate);

        oneOnOneInquiryService.answer(oneOnOneInquiryPostKey, answerRequest, adminIdFromSession);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/v1/one-on-one-inquiry-posts/{key}")
    @Operation(summary = "1:1문의 > 삭제")
    public ResponseEntity<?> deleteInquiryPost(@PathVariable("key") Long oneOnOneInquiryPostKey, HttpServletRequest httpServletRequest) throws DecoderException, IOException {

        oneOnOneInquiryService.deleteInquiryPost(oneOnOneInquiryPostKey);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/v1/one-on-one-inquiry-posts")
    @Operation(summary = "1:1문의.목록 > 조회")
    public ResponseEntity<?> findOneOnOneInquiryPostDetailsListByParentKey(Pageable pageable)  {

        OneOnOneInquiryPostDetailsListResponse oneOnOneInquiryPostDetailsListResponse = oneOnOneInquiryService.findOneOnOneInquiryPostDetailsListByParentKey(pageable);

        Map<String, Object> result = new HashMap<>();
        result.put("databody", oneOnOneInquiryPostDetailsListResponse);
        return new ResponseEntity(result, HttpStatus.OK);
    }

    @GetMapping("/v1/one-on-one-inquiry-posts/{key}")
    @Operation(summary = "1:1문의.상세 > 조회")
    public ResponseEntity<?> findOneOnOneInquiryPostDetailsByKey(@PathVariable("key") Long oneOnOneInquiryPostKey)  {

        OneOnOneInquiryPostDetailsResponse oneOnOneInquiryPostDetailsResponse =
                oneOnOneInquiryService.findOneOnOneInquiryPostDetailsByKey(oneOnOneInquiryPostKey);

        Map<String, Object> result = new HashMap<>();
        result.put("databody", oneOnOneInquiryPostDetailsResponse);
        return new ResponseEntity(result, HttpStatus.OK);
    }
}
