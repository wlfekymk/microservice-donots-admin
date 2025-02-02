package com.kyobo.platform.donots.service;

import com.kyobo.platform.donots.common.exception.BusinessException;
import com.kyobo.platform.donots.common.exception.DataNotFoundException;
import com.kyobo.platform.donots.common.exception.DefaultException;
import com.kyobo.platform.donots.common.util.S3FileUploadUtil;
import com.kyobo.platform.donots.config.HttpConfig;
import com.kyobo.platform.donots.model.dto.request.NoticeRequest;
import com.kyobo.platform.donots.model.dto.response.NoticeListResponse;
import com.kyobo.platform.donots.model.dto.response.NoticeResponse;
import com.kyobo.platform.donots.model.entity.NoticePost;
import com.kyobo.platform.donots.model.repository.NoticePostRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;
import org.json.simple.JSONObject;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NoticeService {

    private NoticePostRepository noticePostRepository;
    private S3FileUploadUtil s3FileUploadUtil;

    private String recipeUrl;

    private Environment environment;

    public NoticeService(NoticePostRepository noticePostRepository, S3FileUploadUtil s3FileUploadUtil, Environment environment) throws IOException {
        this.noticePostRepository = noticePostRepository;
        this.s3FileUploadUtil = s3FileUploadUtil;

        this.environment = environment;
        recipeUrl = loadProperty().getProperty("recipeurl");
    }

    public Properties loadProperty() throws IOException {
        log.info("S3FileUploadUtil.loadProperty");

        if (environment == null)
            throw new BusinessException("S3FileUploadUtil.environment field is failed to be injected. (의존성 주입 실패)");

        String profileToUseProperties;
        if (environment.getActiveProfiles().length == 0)
            profileToUseProperties = "dev";  // Active Profile이 없을 경우(default일 경우) 개발계 구성을 사용한다. 추후 로컬 구성파일이 별도로 필요할 경우 변경한다.
        else
            profileToUseProperties = environment.getActiveProfiles()[0];

        Properties properties = new Properties();
        Resource newResource = new ClassPathResource("awsAuth-" + profileToUseProperties + ".properties");
        log.info("Currently using [awsAuth-{}.properties]", profileToUseProperties);
        BufferedReader br = new BufferedReader(new InputStreamReader(newResource.getInputStream()));
        properties.load(br);

        return properties;
    }

    @Transactional
    public Long noticeRegedit(NoticeRequest noticeRequest, String adminId, MultipartFile multipartFile) throws DecoderException, IOException {
        NoticePost foundNoticePost = noticePostRepository.save(noticePostRegedit(noticeRequest, adminId));

        // FIXME [TEST] S3 연동 후 주석 제거 및 이미지 업로드, 삭제 테스트
        if (multipartFile != null) {    // 첨부된 파일이 있으면 업로드
            String uploadedImageUrl = uploadNoticeImageToS3AndUpdateUrl(foundNoticePost.getNoticePostKey(), multipartFile);
            foundNoticePost.updateImageUrl(uploadedImageUrl);
        }

        // 홈 > 알림 API 호출 시작
        String url = recipeUrl + "/v1/recipe/registration/registMemberNoti";

        JSONObject newNoticePostNotifRequest = new JSONObject();
        newNoticePostNotifRequest.put("noti_img_key", "");                                      // 사용되지 않는 필드지만 API 스펙에 따라 셋팅
        newNoticePostNotifRequest.put("noti_recipe_key", foundNoticePost.getNoticePostKey());   // 공지사항키
        newNoticePostNotifRequest.put("noti_subject", foundNoticePost.getTitle());              // 알림 메시지
        newNoticePostNotifRequest.put("noti_type", "공지사항");                                 // 알림 유형 (승급, 공지사항)
        newNoticePostNotifRequest.put("noti_target_user_key", "");                              // 알림 대상 회원키 (공지사항 알림에서는 사용되지 않음)

        JSONObject newNoticePostNotifResponse = new HttpConfig().callApi(newNoticePostNotifRequest, url, HttpMethod.POST.name());
        if (newNoticePostNotifResponse == null) {
            log.info("Recipe API 처리시 오류 발생 (newNoticePostNotifResponse == null)");
            throw new DefaultException("Recipe API 처리시 오류 발생 (newNoticePostNotifResponse == null)");
        }

        String newNoticePostNotifResponseValue = (String) newNoticePostNotifResponse.get("databody");
        log.info("newNoticePostNotifResponseValue: " + newNoticePostNotifResponseValue);
        if (newNoticePostNotifResponseValue == null) {
            log.info("Recipe API 처리시 오류 발생 (ResponseBody(응답받은 JSON)에 databody 속성이 없음)");
            throw new DefaultException("Recipe API 처리시 오류 발생 (newNoticePostNotifResponseValue == null)");
        }
        // 홈 > 알림 API 호출 끝

        return foundNoticePost.getNoticePostKey();
    }

    public NoticeListResponse findNoticePostsFiltered(String searchTerm, Pageable pageable) {

        Page<NoticePost> postPage;
        if (StringUtils.hasText(searchTerm))
            postPage = noticePostRepository.findByTitleContainingOrBodyContainingOrderByCreatedDateDesc(searchTerm, searchTerm, pageable);
        else
            postPage = noticePostRepository.findAllByOrderByCreatedDateDesc(pageable);

        List<NoticeResponse> noticeResponseList = postPage.getContent().stream()
                .map(m -> new NoticeResponse(m))
                .collect(Collectors.toList());

        NoticeListResponse response = new NoticeListResponse(noticeResponseList, postPage.getTotalPages(), postPage.getTotalElements());

        return response;
    }

    public NoticeResponse getNoticeDetail(Long noticePostKey) {
        NoticePost noticePost = noticePostRepository.findById(noticePostKey)
                .orElseThrow(() -> new DataNotFoundException());
        return new NoticeResponse(noticePost);
    }

    public void deleteNotice(Long noticePostKey) throws IOException {
        // TODO 해당 공지사항 없을 때 예외처리
        noticePostRepository.deleteById(noticePostKey);

        // 홈 > 알림 API 호출 시작
        String url = recipeUrl + "/v1/recipe/main/deleteNoti/" + noticePostKey;
        new HttpConfig().callApi(new JSONObject(), url, HttpMethod.DELETE.name());
        // 홈 > 알림 API 호출 끝
    }

    @Transactional
    public void updateNotice(Long noticePostKey, NoticeRequest noticeRequest, MultipartFile multipartFile) throws DecoderException, IOException {
        NoticePost foundNoticePost = noticePostRepository.findById(noticePostKey)
                .orElseThrow(() -> new DataNotFoundException());

        foundNoticePost.updateNotice(noticeRequest.getTitle(), noticeRequest.getBody(), noticeRequest.getBoardStartDate(), noticeRequest.getBoardEndDate());

        // FIXME [TEST] S3 연동 후 주석 제거 및 이미지 업로드, 삭제 테스트
        if (noticeRequest.getIsAttachedImageFileChanged()) {    // 클라이언트에서 첨부파일이 변경된 적이 있고,
            if (multipartFile != null) {    // 첨부된 파일이 있으면 업로드(교체)
                String uploadedImageUrl = uploadNoticeImageToS3AndUpdateUrl(foundNoticePost.getNoticePostKey(), multipartFile);
                foundNoticePost.updateImageUrl(uploadedImageUrl);
            }
            // TODO 클라이언트에서 null을 제대로 넘겨주는지 확인 필요. 필드를 아예 안 넣는 것과 비교가 되어야 할 듯
            else {  // 첨부된 파일이 null이면 삭제
                deleteNoticeImageFromS3AndUpdateUrl(foundNoticePost.getNoticePostKey());
                foundNoticePost.updateImageUrl("");
            }
        }
    }

    private NoticePost noticePostRegedit(NoticeRequest noticeRequest, String adminId) {
        LocalDateTime now = LocalDateTime.now();
        NoticePost noticePost = NoticePost.builder()
                .title(noticeRequest.getTitle())
                .body(noticeRequest.getBody())
                .boardStartDate(noticeRequest.getBoardStartDate())
                .boardEndDate(noticeRequest.getBoardEndDate())
                .adminId(adminId)
                .createdDate(now)
                .lastModifiedDate(now)
                .build();
        return noticePost;
    }

    @Transactional
    public String uploadNoticeImageToS3AndUpdateUrl(Long key, MultipartFile multipartFile) throws DecoderException, IOException {
        NoticePost foundNoticePost = noticePostRepository.findById(key).orElseThrow(() -> new DefaultException("요청된 공지사항이 없습니다."));
        String foundImageUrl = foundNoticePost.getImageUrl();
        String imageDirectoryPathAfterDomain = "notice-posts/" + key + "/";
        String uploadedImageUrl = s3FileUploadUtil.uploadImageToS3AndGetUrl(multipartFile, foundImageUrl, imageDirectoryPathAfterDomain);
        foundNoticePost.updateImageUrl(uploadedImageUrl);
        return uploadedImageUrl;
    }

    @Transactional
    public void deleteNoticeImageFromS3AndUpdateUrl(Long key) throws DecoderException, IOException {
        NoticePost foundNoticePost = noticePostRepository.findById(key).orElseThrow(() -> new DefaultException("요청된 공지사항이 없습니다."));
        String imageDirectoryPathAfterDomain = "notice-posts/" + key + "/";
        s3FileUploadUtil.deleteImageFromS3(foundNoticePost.getImageUrl(), imageDirectoryPathAfterDomain);
        foundNoticePost.updateImageUrl("");
    }
}
