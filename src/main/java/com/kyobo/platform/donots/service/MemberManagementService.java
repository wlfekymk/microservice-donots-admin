package com.kyobo.platform.donots.service;

import com.kyobo.platform.donots.common.exception.ParentNotFoundException;
import com.kyobo.platform.donots.common.util.SessionUtil;
import com.kyobo.platform.donots.model.dto.response.ParentAccountDetailsResponse;
import com.kyobo.platform.donots.model.dto.response.ParentAccountResponse;
import com.kyobo.platform.donots.model.entity.AdminUser;
import com.kyobo.platform.donots.model.entity.PersonalInfoAccessHistory;
import com.kyobo.platform.donots.model.entity.service.account.Account;
import com.kyobo.platform.donots.model.entity.service.account.SocialAccount;
import com.kyobo.platform.donots.model.entity.service.parent.Parent;
import com.kyobo.platform.donots.model.entity.service.parent.ParentType;
import com.kyobo.platform.donots.model.repository.AdminUserRepository;
import com.kyobo.platform.donots.model.repository.PersonalInfoAccessHistoryRepository;
import com.kyobo.platform.donots.model.repository.searchcondition.ParentAccountSearchConditionAndTerm;
import com.kyobo.platform.donots.model.repository.service.account.AccountRepository;
import com.kyobo.platform.donots.model.repository.service.account.SocialAccountRepository;
import com.kyobo.platform.donots.model.repository.service.parent.ParentRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberManagementService {
    private final AdminUserRepository adminUserRepository;
    private final ParentRepository parentRepository;
    private final AccountRepository accountRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final PersonalInfoAccessHistoryRepository personalInfoAccessHistoryRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Transactional
    public Page<ParentAccountResponse> findAllMembersWithAccount(ParentAccountSearchConditionAndTerm searchConditionAndTerm, Pageable pageable) {
        log.info("MemberManagementService.findAllMembersWithAccount");
        Page<ParentAccountResponse> parentWithAccountResponses = parentRepository.search(searchConditionAndTerm, pageable);
        return parentWithAccountResponses;
    }

    @Transactional
    public ParentAccountDetailsResponse findParentAccountDetails(Long parentKey, HttpServletRequest httpServletRequest) throws Exception {
        log.info("MemberManagementService.findParentAccountDetails");

        Parent foundParent = parentRepository.findById(parentKey).orElseThrow(() -> new ParentNotFoundException());

        // TODO 예외처리
        Account foundAccount = accountRepository.findByAccountKey(foundParent.getAccountKey());
        List<SocialAccount> foundSocialAccounts = socialAccountRepository.findByAccountKey(foundParent.getAccountKey());

        String regeditAdminId = SessionUtil.getGlobalCustomSessionStringAttribute("adminId", httpServletRequest, redisTemplate);
        AdminUser adminUser = adminUserRepository.findByAdminId(regeditAdminId);
        regeditPersonalInfoAccessHistory(foundParent.getAccountKey(), adminUser, "/v1/members/with-account/{parentKey}/details", "회원정보 조회");

        return ParentAccountDetailsResponse.from(foundParent, foundAccount, foundSocialAccounts);
    }

    @Transactional
    public void modifyParentType(Long key, ParentType parentType, HttpServletRequest httpServletRequest) {
        log.info("MemberManagementService.modifyParentType");

        Parent foundParent = parentRepository.findById(key).orElseThrow(() -> new ParentNotFoundException());
        foundParent.setType(parentType);

        String regeditAdminId = SessionUtil.getGlobalCustomSessionStringAttribute("adminId", httpServletRequest, redisTemplate);
        AdminUser adminUser = adminUserRepository.findByAdminId(regeditAdminId);
        regeditPersonalInfoAccessHistory(foundParent.getAccountKey(), adminUser, "/v1/members/{key}/type", "회원등급 변경");
    }

    /**
     * 개인정보 처리 로그 등록 
     * @param accountKey 계정 정보
     * @param adminUser adminUser 정보
     * @param apiName 처리한 API 명
     * @param content 처리한 내용
     */
    private void regeditPersonalInfoAccessHistory(Long accountKey, AdminUser adminUser, String apiName, String content ){
        personalInfoAccessHistoryRepository.save(
                PersonalInfoAccessHistory.builder()
                        .inquiredAccountKey(accountKey)
                        .operatorAdminId(adminUser.getAdminId())
                        .operatorAdminUserName(adminUser.getAdminUserName())
                        .operatorAdminUserNumber(adminUser.getAdminUserNumber())
                        .processingContent(content)
                        .processingApiName(apiName)
                        .createdDate(LocalDateTime.now())
                        .build()
        );
    }
}
