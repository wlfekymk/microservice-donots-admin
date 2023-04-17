package com.kyobo.platform.donots.service;


import com.kyobo.platform.donots.common.exception.*;
import com.kyobo.platform.donots.common.util.SessionUtil;
import com.kyobo.platform.donots.model.dto.request.*;
import com.kyobo.platform.donots.model.dto.response.AdminUserDetailResponse;
import com.kyobo.platform.donots.model.dto.response.AdminUserListResponse;
import com.kyobo.platform.donots.model.dto.response.AdminUserResponse;
import com.kyobo.platform.donots.model.entity.AdminSystemAccessLog;
import com.kyobo.platform.donots.model.entity.AdminUser;
import com.kyobo.platform.donots.model.repository.AdminSystemAccessLogRepository;
import com.kyobo.platform.donots.model.repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class LoginService implements UserDetailsService {

    private final AdminUserRepository adminUserRepository;

    private final AdminSystemAccessLogRepository adminSystemAccessLogRepository;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    private final RedisTemplate<String, Object> redisTemplate;

    @Transactional
    public AdminUserResponse createAdminUser(CreateAdminUserRequest createAdminUserRequest, HttpServletRequest httpServletRequest) {

        Long adminUserKeyFromSession = SessionUtil.getGlobalCustomSessionLongAttribute("id", httpServletRequest, redisTemplate);
        checkIsAdminUserPermitted(adminUserKeyFromSession);

        // TODO existBy로 중복검사 후 새로 선언하여 받는다
        AdminUser adminUserToCreate = adminUserRepository.findByAdminId(createAdminUserRequest.getAdminId());
        if (adminUserToCreate != null)
            throw new AlreadyRegisteredIdException();

        LocalDateTime now = LocalDateTime.now();
        String adminIdFromSession = SessionUtil.getGlobalCustomSessionStringAttribute("adminId", httpServletRequest, redisTemplate);

        // 유저정보 저장
        adminUserRepository.save(
                AdminUser.builder()
                        .adminId(createAdminUserRequest.getAdminId())
                        .password(encoder.encode(createAdminUserRequest.getPassword()))
                        .adminUserName(createAdminUserRequest.getAdminUserName())
                        .adminUserNumber(createAdminUserRequest.getAdminUserNumber())
                        .departmentName(createAdminUserRequest.getDepartmentName())
                        .phoneNumber(createAdminUserRequest.getPhoneNumber())
                        .regeditAdminId(adminIdFromSession)
                        .email(createAdminUserRequest.getEmail())
                        .reasonsForAuthorization(createAdminUserRequest.getReasonsForAuthorization())
                        .role(createAdminUserRequest.getRole())
                        .attachImageUrl(createAdminUserRequest.getAttachImageUrl())
                        .memo(createAdminUserRequest.getMemo())
                        .loginCount(0l)
                        .lastPasswordChangeDate(now)
                        .createdDate(now)
                        .lastSignInDate(now)
                        .build()
        );

        return new AdminUserResponse(adminUserToCreate);
    }

    @Transactional
    public void changePasswordRequest(ChangePasswordRequest changePasswordRequest, String adminId) {
        AdminUser adminUser = adminUserRepository.findByAdminId(adminId);
        if (adminUser == null)
            throw new AdminUserNotFoundException();
        if (!encoder.matches(changePasswordRequest.getPassword(), adminUser.getPassword()))
            throw new PasswordNotMatchException();
        if (changePasswordRequest.getNewPassword().contains(adminId))
            throw new PasswordIncludePersonalInformation();
        if (changePasswordRequest.getNewPassword().contains(adminUser.getPhoneNumber()))
            throw new PasswordIncludePersonalInformation();
        adminUser.updatePassword(encoder.encode(changePasswordRequest.getNewPassword()));
    }

    public Map<String, Boolean> verification(String adminId) {
        Map<String, Boolean> result = new HashMap<>();
        boolean verification = adminUserRepository.existsByAdminId(adminId);
        result.put("verification", verification);
        return result;
    }

    @Transactional
    public void deleteAdminUser(Long id, HttpServletRequest httpServletRequest) {

        Long adminUserKeyFromSession = SessionUtil.getGlobalCustomSessionLongAttribute("id", httpServletRequest, redisTemplate);
        checkIsAdminUserPermitted(adminUserKeyFromSession);

        adminUserRepository.deleteById(id);
    }

    @Transactional
    public AdminUserResponse modifyAdminUser(ModifyAdminUserRequest modifyAdminUserRequest, HttpServletRequest httpServletRequest) {

        Long adminUserKeyFromSession = SessionUtil.getGlobalCustomSessionLongAttribute("id", httpServletRequest, redisTemplate);
        checkIsAdminUserPermitted(adminUserKeyFromSession);

        AdminUser adminUser = adminUserRepository.findByAdminId(modifyAdminUserRequest.getAdminId());

        if (adminUser == null)
            throw new AdminUserNotFoundException();

        adminUser.updateModifyAdminUser(modifyAdminUserRequest);

        // TODO 레코드가 커밋되지 않았는데도 Reponse를 할 가능성이 있음
        return new AdminUserResponse(adminUser);
    }


    @Transactional
    public AdminUserResponse signIn(SignInRequest signInRequest) {
        AdminUser adminUser = adminUserRepository.findByAdminId(signInRequest.getAdminId());
        if (adminUser == null)
            throw new AdminUserNotFoundException();
        adminUser.increaseCount(adminUser.getLoginCount());
        if (!encoder.matches(signInRequest.getPassword(), adminUser.getPassword())) {
            // 시스템 접근 로그 실패 저장
            insertAdminSystem(adminUser, false);
            log.info("Lock status : " + adminUser.isAccountNonLocked());
            if (adminUser.getLoginCount() == 5) {
                throw new PasswordFiveCountNotMatchException();
            }
            throw new PasswordNotMatchException();
        }
        adminUser.updateSessionId(adminUser.getSessionId());

        // 시스템 접근 로그 성공 저장
        insertAdminSystem(adminUser, true);

        SessionUtil.populateGlobalCustomSession(redisTemplate, adminUser);

        return new AdminUserResponse(adminUser);
    }

    private void insertAdminSystem(AdminUser adminUser, Boolean loginFlag) {
        adminSystemAccessLogRepository.save(
                AdminSystemAccessLog.builder()
                        .adminUserNumber(adminUser.getAdminUserNumber())
                        .adminUserName(adminUser.getAdminUserName())
                        .adminId(adminUser.getAdminId())
                        .loginFlag(loginFlag)
                        .accessDate(LocalDateTime.now())
                        .build()
        );
    }

    public AdminUserResponse loadUserByAdminId(String adminId) throws UsernameNotFoundException {
        AdminUser adminUser = adminUserRepository.findByAdminId(adminId);
        return new AdminUserResponse(adminUser);
    }


    public AdminUserDetailResponse loadUserById(Long id, HttpServletRequest httpServletRequest) throws UsernameNotFoundException {

        Long adminUserKeyFromSession = SessionUtil.getGlobalCustomSessionLongAttribute("id", httpServletRequest, redisTemplate);
        checkIsAdminUserPermitted(adminUserKeyFromSession);

        AdminUser adminUser = adminUserRepository.findById(id).orElseThrow(() -> new AdminUserNotFoundException());
        return new AdminUserDetailResponse(adminUser);
    }

    public AdminUserListResponse getAdminUserAll(String search, Pageable pageable, AdminUserSearchType type, HttpServletRequest httpServletRequest) {

        Long adminUserKeyFromSession = SessionUtil.getGlobalCustomSessionLongAttribute("id", httpServletRequest, redisTemplate);
        checkIsAdminUserPermitted(adminUserKeyFromSession);

        Page<AdminUser> pageAdminUser;
        if (type.equals(AdminUserSearchType.ADMIN_ID)) {
            pageAdminUser = adminUserRepository.findByAdminIdContaining(search, pageable);
        } else if (type.equals(AdminUserSearchType.ADMIN_ROLE)) {
            pageAdminUser = adminUserRepository.findByRole(search, pageable);
        } else if (type.equals(AdminUserSearchType.REGEDIT_ADMIN_ID)) {
            pageAdminUser = adminUserRepository.findByRegeditAdminIdContaining(search, pageable);
        } else if (type.equals(AdminUserSearchType.ALL)) {
            pageAdminUser = adminUserRepository.findAll(pageable);
        } else {
            pageAdminUser = adminUserRepository.findAll(pageable);
        }


        List<AdminUserResponse> adminUserList = pageAdminUser.getContent().stream()
                .map(m -> new AdminUserResponse(m))
                .collect(Collectors.toList());

        AdminUserListResponse response = new AdminUserListResponse(adminUserList, pageAdminUser.getTotalPages(), pageAdminUser.getTotalElements());

        return response;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return null;
    }

    @Transactional
    public void passwordInitialization(Long id) {
        AdminUser adminUser = adminUserRepository.findById(id).orElseThrow(() -> new AdminUserNotFoundException());
        adminUser.updateLastPasswordChangeDate();
    }

    private void checkIsAdminUserPermitted(Long adminUserKeyFromSession) {
        AdminUser foundAdminUser = adminUserRepository.findById(adminUserKeyFromSession).orElseThrow(() -> new AdminUserNotFoundException());
        if (!"SUPER_ADMIN".equals(foundAdminUser.getRole()))
            throw new InsufficientPermissionException();
    }
}