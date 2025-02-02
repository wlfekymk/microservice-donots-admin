package com.kyobo.platform.donots.service;

import com.kyobo.platform.donots.common.exception.AdminUserNotFoundException;
import com.kyobo.platform.donots.common.exception.AlreadyRegisteredIdException;
import com.kyobo.platform.donots.common.exception.InsufficientPermissionException;
import com.kyobo.platform.donots.common.exception.PasswordNotMatchException;
import com.kyobo.platform.donots.common.util.SessionUtil;
import com.kyobo.platform.donots.model.dto.request.AdminUserSearchType;
import com.kyobo.platform.donots.model.dto.request.CreateAdminUserRequest;
import com.kyobo.platform.donots.model.dto.request.ModifyAdminUserRequest;
import com.kyobo.platform.donots.model.dto.request.PasswordUnlockRequest;
import com.kyobo.platform.donots.model.dto.response.AdminUserDetailResponse;
import com.kyobo.platform.donots.model.dto.response.AdminUserListResponse;
import com.kyobo.platform.donots.model.dto.response.AdminUserResponse;

import com.kyobo.platform.donots.model.entity.AdminAccessPermission;
import com.kyobo.platform.donots.model.entity.AdminUser;
import com.kyobo.platform.donots.model.entity.PermissionCategory;
import com.kyobo.platform.donots.model.repository.AdminAccessPermissionRepository;
import com.kyobo.platform.donots.model.repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SuperAdminService {
    private final AdminUserRepository adminUserRepository;
    private final AdminAccessPermissionRepository adminAccessPermissionRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 관리자 정보 생성
     *
     * @param createAdminUserRequest
     * @param httpServletRequest
     * @return
     */

    @Transactional
    public AdminUserResponse createAdminUser(CreateAdminUserRequest createAdminUserRequest, HttpServletRequest httpServletRequest) {
        Long adminUserKeyFromSession = SessionUtil.getGlobalCustomSessionLongAttribute("id", httpServletRequest, redisTemplate);
        String regeditAdminId = SessionUtil.getGlobalCustomSessionStringAttribute("adminId", httpServletRequest, redisTemplate);
        checkIsAdminUserPermitted(adminUserKeyFromSession);

        // TODO existBy로 중복검사 후 새로 선언하여 받는다
        AdminUser adminUser = adminUserRepository.findByAdminId(createAdminUserRequest.getAdminId());
        if (adminUser != null)
            throw new AlreadyRegisteredIdException();

        LocalDateTime now = LocalDateTime.now();

        // 유저정보 저장
        adminUser = adminUserRepository.save(
                AdminUser.builder()
                        .adminId(createAdminUserRequest.getAdminId())
                        .password(encoder.encode(createAdminUserRequest.getPassword()))
                        .adminUserName(createAdminUserRequest.getAdminUserName())
                        .adminUserNumber(createAdminUserRequest.getAdminUserNumber())
                        .departmentName(createAdminUserRequest.getDepartmentName())
                        .phoneNumber(createAdminUserRequest.getPhoneNumber())
                        .regeditAdminId(regeditAdminId)
                        .email(createAdminUserRequest.getEmail())
                        .reasonsForAuthorization(createAdminUserRequest.getReasonsForAuthorization())
                        .role(createAdminUserRequest.getRole())
                        .attachImageUrl(createAdminUserRequest.getAttachImageUrl())
                        .memo(createAdminUserRequest.getMemo())
                        .loginCount(0l)
                        .loginFailedCount(0l)
                        .lastPasswordChangeDate(now)
                        .createdDate(now)
                        .lastSignInDate(now)
                        .build()
        );

        AdminUser regeditAdmin = adminUserRepository.findByAdminId(regeditAdminId);
        //ADMIN 권한 이력 관리
        regeditAdminAccessPermission(adminUser, regeditAdmin, PermissionCategory.C, createAdminUserRequest.getRole() + "으로 Role 생성");
        return new AdminUserResponse(adminUser);
    }

    /**
     * 관리자 정보 변경
     *
     * @param modifyAdminUserRequest
     * @param httpServletRequest
     * @return
     */
    @Transactional
    public AdminUserResponse modifyAdminUser(ModifyAdminUserRequest modifyAdminUserRequest, HttpServletRequest httpServletRequest) {
        Long adminUserKeyFromSession = SessionUtil.getGlobalCustomSessionLongAttribute("id", httpServletRequest, redisTemplate);
        String regeditAdminId = SessionUtil.getGlobalCustomSessionStringAttribute("adminId", httpServletRequest, redisTemplate);
        checkIsAdminUserPermitted(adminUserKeyFromSession);
        AdminUser adminUser = adminUserRepository.findByAdminId(modifyAdminUserRequest.getAdminId());
        if (adminUser == null)
            throw new AdminUserNotFoundException();
        adminUser.updateModifyAdminUser(modifyAdminUserRequest);

        AdminUser regeditAdmin = adminUserRepository.findByAdminId(regeditAdminId);
        //ADMIN 권한 이력 관리
        regeditAdminAccessPermission(adminUser, regeditAdmin, PermissionCategory.U, modifyAdminUserRequest.getRole() + "으로 Role 변경");
        return new AdminUserResponse(adminUser);
    }

    /**
     * 관리자 정보 삭제
     * @param id
     * @param httpServletRequest
     */
    @Transactional
    public void deleteAdminUser(Long id, HttpServletRequest httpServletRequest) {
        Long adminUserKeyFromSession = SessionUtil.getGlobalCustomSessionLongAttribute("id", httpServletRequest, redisTemplate);
        String regeditAdminId = SessionUtil.getGlobalCustomSessionStringAttribute("adminId", httpServletRequest, redisTemplate);
        checkIsAdminUserPermitted(adminUserKeyFromSession);
        AdminUser adminUser = adminUserRepository.findById(id).orElseThrow(() -> new AdminUserNotFoundException());
        adminUserRepository.deleteById(id);
        AdminUser regeditAdmin = adminUserRepository.findByAdminId(regeditAdminId);
        //ADMIN 권한 이력 관리
        regeditAdminAccessPermission(adminUser, regeditAdmin, PermissionCategory.D, "");
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
        //regeditAdminAccessPermission(adminUser, PermissionCategory.R);
        return response;
    }

    /** 관리자 정보 조회
     * 
     * @param id
     * @param httpServletRequest
     * @return
     * @throws UsernameNotFoundException
     */
    public AdminUserDetailResponse loadUserById(Long id, HttpServletRequest httpServletRequest) throws UsernameNotFoundException {
        Long adminUserKeyFromSession = SessionUtil.getGlobalCustomSessionLongAttribute("id", httpServletRequest, redisTemplate);
        String regeditAdminId = SessionUtil.getGlobalCustomSessionStringAttribute("adminId", httpServletRequest, redisTemplate);
        checkIsAdminUserPermitted(adminUserKeyFromSession);
        AdminUser adminUser = adminUserRepository.findById(id).orElseThrow(() -> new AdminUserNotFoundException());
        AdminUser regeditAdmin = adminUserRepository.findByAdminId(regeditAdminId);
        //ADMIN 권한 이력 관리
        regeditAdminAccessPermission(adminUser, regeditAdmin, PermissionCategory.R, "");
        return new AdminUserDetailResponse(adminUser);
    }

    /**
     * 관리자 아이디 가입여부 확인
     * @param adminId
     * @return
     */
    public Map<String, Boolean> verification(String adminId) {
        Map<String, Boolean> result = new HashMap<>();
        boolean verification = adminUserRepository.existsByAdminId(adminId);
        result.put("verification", verification);
        return result;
    }

    /**
     * SUPER_ADMIN 권한인지 체크
     * @param adminUserKey
     */
    private void checkIsAdminUserPermitted(Long adminUserKey) {
        AdminUser adminUser = adminUserRepository.findById(adminUserKey).orElseThrow(() -> new AdminUserNotFoundException());
        if (!"SUPER_ADMIN".equals(adminUser.getRole()))
            throw new InsufficientPermissionException();
    }

    /**
     *
     * @param adminUser
     * @param regeditAdminUser
     * @param permissionCategory
     * @param changePermission
     */
    private void regeditAdminAccessPermission(AdminUser adminUser, AdminUser regeditAdminUser, PermissionCategory permissionCategory, String changePermission ) {
        adminAccessPermissionRepository.save(
                AdminAccessPermission.builder()
                        .adminId(adminUser.getAdminId())
                        .adminUserName(adminUser.getAdminUserName())
                        .adminUserNumber(adminUser.getAdminUserNumber())
                        .departmentName(adminUser.getDepartmentName())
                        .regeditAdminId(regeditAdminUser.getRegeditAdminId())
                        .regeditAdminUserName(regeditAdminUser.getAdminUserName())
                        .regeditAdminUserNumber(regeditAdminUser.getAdminUserNumber())
                        .permissionCategory(permissionCategory)
                        .changePermission(changePermission)
                        .createdDate(LocalDateTime.now())
                        .build()
        );
    }

    /**
     * 패스워드 잠금 해제
     * @param passwordUnlockRequest
     * @param httpServletRequest
     */
    @Transactional
    public void passwordUnlock(PasswordUnlockRequest passwordUnlockRequest, HttpServletRequest httpServletRequest) {
        String regeditAdminId = SessionUtil.getGlobalCustomSessionStringAttribute("adminId", httpServletRequest, redisTemplate);

        AdminUser regeditAdminUser = adminUserRepository.findByAdminId(regeditAdminId);
        if (regeditAdminUser == null)
            throw new AdminUserNotFoundException();

        if (!encoder.matches(passwordUnlockRequest.getRegeditAdminPassword(), regeditAdminUser.getPassword())) {
            throw new PasswordNotMatchException();
        }

        AdminUser adminUser = adminUserRepository.findByAdminId(passwordUnlockRequest.getAdminId());

        if (adminUser == null)
            throw new AdminUserNotFoundException();

        adminUser.updateLoginFailedCountReset();
        adminUser.updatePassword(encoder.encode("kyobo11!"));

        //ADMIN 권한 이력 관리
        regeditAdminAccessPermission(adminUser, regeditAdminUser, PermissionCategory.U, "Password Unlock");
    }
}
