package com.kyobo.platform.donots.service;

import com.kyobo.platform.donots.common.exception.*;
import com.kyobo.platform.donots.common.util.SessionUtil;
import com.kyobo.platform.donots.model.dto.request.*;
import com.kyobo.platform.donots.model.dto.response.AdminUserResponse;
import com.kyobo.platform.donots.model.entity.AdminSystemAccessLog;
import com.kyobo.platform.donots.model.entity.AdminUser;
import com.kyobo.platform.donots.model.repository.AdminSystemAccessLogRepository;
import com.kyobo.platform.donots.model.repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
@Slf4j
public class LoginService implements UserDetailsService {

    private final AdminUserRepository adminUserRepository;
    private final AdminSystemAccessLogRepository adminSystemAccessLogRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final RedisTemplate<String, Object> redisTemplate;

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

    @Transactional
    public AdminUserResponse signIn(SignInRequest signInRequest) {
        AdminUser adminUser = adminUserRepository.findByAdminId(signInRequest.getAdminId());
        if (adminUser == null)
            throw new AdminUserNotFoundException();
        adminUser.increaseCount(adminUser.getLoginCount());
        if (!encoder.matches(signInRequest.getPassword(), adminUser.getPassword())) {
            // 시스템 접근 로그 실패 저장
            regeditAdminSystemLog(adminUser, false);
            log.info("Lock status : " + adminUser.isAccountNonLocked());
            if (adminUser.getLoginCount() == 5) {
                throw new PasswordFiveCountNotMatchException();
            }
            throw new PasswordNotMatchException();
        }
        adminUser.updateSessionId(adminUser.getSessionId());
        // 시스템 접근 로그 성공 저장
        regeditAdminSystemLog(adminUser, true);
        SessionUtil.populateGlobalCustomSession(redisTemplate, adminUser);
        return new AdminUserResponse(adminUser);
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

    /**
     * 시스템 접근 로그
     * @param adminUser
     * @param loginFlag
     */
    private void regeditAdminSystemLog(AdminUser adminUser, Boolean loginFlag) {
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
}