package com.kyobo.platform.donots.service;

import com.kyobo.platform.donots.common.util.S3FileUploadUtil;
import com.kyobo.platform.donots.model.dto.request.CreatePushRequest;
import com.kyobo.platform.donots.model.dto.request.PushSearchType;
import com.kyobo.platform.donots.model.dto.request.PushSendListRequest;
import com.kyobo.platform.donots.model.dto.response.PushSendDetailResponse;
import com.kyobo.platform.donots.model.dto.response.PushSendListResponse;
import com.kyobo.platform.donots.model.entity.Push;
import com.kyobo.platform.donots.model.repository.PushRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PushService {

    private final PushRepository pushRepository;

    private final S3FileUploadUtil s3FileUploadUtil;

    /**
     * Push 발송 등록
     *
     * @param createPushRequest
     * @param adminId
     * @param multipartFile
     * @return
     */
    @Transactional
    public Long pushSendRegedit(CreatePushRequest createPushRequest, String adminId, MultipartFile multipartFile) throws DecoderException, IOException {

        Push push = Push.builder()
                .title(createPushRequest.getTitle())
                .regeditAdminId(adminId)
                .contentBody(createPushRequest.getContentBody())
                .reservationDate(createPushRequest.getReservationDate())
                .isSendFlag(false)
                .regeditDate(LocalDateTime.now())
                .build();
        push = pushRepository.save(push);

        if (multipartFile != null) {
            String imageUrl = upLoadImageToS3(push.getId(), multipartFile, "");
            push.updateImageInfo(multipartFile.getOriginalFilename(), imageUrl);
        }

        return push.getId();
    }


    public PushSendListResponse pushSendList(PushSendListRequest pushSendListRequest, Pageable pageable) {
        Page<Push> pushPage = null;

        LocalDateTime start = null;
        LocalDateTime end = null;
        if (pushSendListRequest.getStart() != null)
            start = convertStringForLocalDateTime(pushSendListRequest.getStart());
        if (pushSendListRequest.getEnd() != null)
            end = convertStringForLocalDateTime(pushSendListRequest.getEnd());

        if (pushSendListRequest.getSearchType().equals(PushSearchType.ALL)) {
            if (start != null || end != null)
                pushPage = pushRepository.findByRegeditDateBetween(start, end, pageable);
            else
                pushPage = pushRepository.findAll(pageable);
        } else if (pushSendListRequest.getSearchType().equals(PushSearchType.REGEDIT)) {
            if (start != null || end != null)
                pushPage = pushRepository.findByRegeditDateBetweenAndRegeditAdminIdContaining(start, end, pushSendListRequest.getSearchWord(), pageable);
            else
                pushPage = pushRepository.findByRegeditAdminIdContaining(pushSendListRequest.getSearchWord(), pageable);
        } else if (pushSendListRequest.getSearchType().equals(PushSearchType.TITLE)) {
            if (start != null || end != null)
                pushPage = pushRepository.findByRegeditDateBetweenAndTitleContaining(start, end, pushSendListRequest.getSearchWord(), pageable);
            else
                pushPage = pushRepository.findByTitleContaining(pushSendListRequest.getSearchWord(), pageable);
        } else if (pushSendListRequest.getSearchType().equals(PushSearchType.FILENAME)) {
            if (start != null || end != null)
                pushPage = pushRepository.findByRegeditDateBetweenAndAttachFileNameContaining(start, end, pushSendListRequest.getSearchWord(), pageable);
            else
                pushPage = pushRepository.findByAttachFileNameContaining(pushSendListRequest.getSearchWord(), pageable);
        }

        List<PushSendDetailResponse> list = pushPage.getContent().stream()
                .map(m -> new PushSendDetailResponse(m))
                .collect(Collectors.toList());

        return new PushSendListResponse(list, pushPage.getTotalPages(), pushPage.getTotalElements());
    }

    private LocalDateTime convertStringForLocalDateTime(String time) {
        time = time + " 00:00:00.000";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        return LocalDateTime.parse(time, formatter);
    }

    public void pushSendDelete(Long id, String adminId) {
        pushRepository.deleteById(id);
    }



    private String upLoadImageToS3(Long key, MultipartFile multipartFile, String beforeImageUrl) throws DecoderException, IOException {
        String imageDirectoryPath = "push/" + key + "/";
        String imageUrl = s3FileUploadUtil.uploadImageToS3AndGetUrl(multipartFile, beforeImageUrl, imageDirectoryPath);
        return imageUrl;
    }

}

