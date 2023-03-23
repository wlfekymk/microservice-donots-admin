package com.kyobo.platform.donots.service;

import com.kyobo.platform.donots.common.exception.AdminUserNotFoundException;
import com.kyobo.platform.donots.common.exception.AlreadyRegisteredIdException;
import com.kyobo.platform.donots.common.exception.PasswordIncludePersonalInformation;
import com.kyobo.platform.donots.common.exception.PasswordNotMatchException;
import com.kyobo.platform.donots.common.util.SessionUtil;
import com.kyobo.platform.donots.model.dto.request.*;
import com.kyobo.platform.donots.model.dto.response.AdminUserListResponse;
import com.kyobo.platform.donots.model.dto.response.AdminUserResponse;
import com.kyobo.platform.donots.model.entity.AdminUser;
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

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
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
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    private final RedisTemplate<String, Object> redisTemplate;

    @PostConstruct
    public void initialize(){
        AdminUser adminUser = adminUserRepository.findByAdminId("superkyobo");
        if(adminUser == null) {
            LocalDateTime now = LocalDateTime.now();
            adminUser = AdminUser.builder()
                .adminId("superkyobo")
                .password(encoder.encode("kyobo11!"))
                .adminUserName("슈퍼유저")
                .adminUserNumber("99999999")
                .departmentName("플랫폼추진2팀")
                .phoneNumber("010-1234-5678")
                .regeditAdminId("superkyobo")
                .email("kyobo@help.com")
                .reasonsForAuthorization("")
                .role("SUPER_ADMIN")
                .attachImageUrl("")
                .memo("")
                .loginCount(0l)
                .lastPasswordChangeDate(now)
                .createdDate(now)
                .lastSignInDate(now)
                .build();
            adminUserRepository.save(adminUser);
        }
    }
    public AdminUserResponse createAdminUser(CreateAdminUserRequest createAdminUserRequest, HttpServletRequest httpServletRequest) {

        HashMap<String, Object> sessionMap = SessionUtil.getGlobalCustomSessionValue(httpServletRequest, redisTemplate);
        String adminIdFromSession = sessionMap.get("adminId").toString();

        AdminUser adminUser = adminUserRepository.findByAdminId(createAdminUserRequest.getAdminId());

        if(adminUser != null)
            throw new AlreadyRegisteredIdException();

        LocalDateTime now = LocalDateTime.now();
        adminUser = AdminUser.builder()
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
                .build();
        adminUserRepository.save(adminUser);

        return new AdminUserResponse(adminUser);
    }

    @Transactional
    public void changePasswordRequest(ChangePasswordRequest changePasswordRequest, String adminId) {
        AdminUser adminUser = adminUserRepository.findByAdminId(adminId);
        if(adminUser == null)
            throw new AdminUserNotFoundException();
        if(!encoder.matches(changePasswordRequest.getPassword(), adminUser.getPassword()))
            throw new PasswordNotMatchException();
        if(changePasswordRequest.getNewPassword().contains(adminId))
            throw new PasswordIncludePersonalInformation();
        if(changePasswordRequest.getNewPassword().contains(adminUser.getPhoneNumber()))
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
    public void deleteAdminUser(Long id) {
        adminUserRepository.deleteById(id);
    }

    @Transactional
    public AdminUserResponse modifyAdminUser(ModifyAdminUserRequest modifyAdminUserRequest){
        AdminUser adminUser = adminUserRepository.findByAdminId(modifyAdminUserRequest.getAdminId());

        if(adminUser == null)
            throw new AdminUserNotFoundException();

        adminUser.updateModifyAdminUser(modifyAdminUserRequest);

        return new AdminUserResponse(adminUser);
    }


    @Transactional
    public AdminUserResponse signIn(SignInRequest signInRequest) {
        AdminUser adminUser = adminUserRepository.findByAdminId(signInRequest.getAdminId());
        if(adminUser == null)
            throw new AdminUserNotFoundException();
        if(!encoder.matches(signInRequest.getPassword(), adminUser.getPassword()))
            throw new PasswordNotMatchException();
        adminUser.increaseCount(adminUser.getLoginCount());
        adminUser.updateSessionId(adminUser.getSessionId());

        SessionUtil.populateGlobalCustomSession(redisTemplate, adminUser);

        return new AdminUserResponse(adminUser);
    }



    public AdminUserResponse loadUserByAmdinId(String adminId) throws UsernameNotFoundException {
        AdminUser adminUser = adminUserRepository.findByAdminId(adminId);
        return new AdminUserResponse(adminUser);
    }


    public AdminUserResponse loadUserById(Long id) throws UsernameNotFoundException {
        AdminUser adminUser = adminUserRepository.findById(id).orElseThrow(()-> new AdminUserNotFoundException());
        return new AdminUserResponse(adminUser);
    }
    public AdminUserListResponse getAdminUserAll(String search, Pageable pageable, AdminUserSearchType type) {
        Page<AdminUser> pageAdminUser;
        if (type.equals(AdminUserSearchType.ADMIN_ID)) {
            pageAdminUser = adminUserRepository.findByAdminIdContaining(search, pageable);
        } else if(type.equals(AdminUserSearchType.ADMIN_ROLE)){
            pageAdminUser = adminUserRepository.findByRole(search, pageable);
        } else if(type.equals(AdminUserSearchType.REGEDIT_ADMIN_ID)) {
            pageAdminUser = adminUserRepository.findByRegeditAdminIdContaining(search, pageable);
        } else if(type.equals(AdminUserSearchType.ALL)) {
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
        AdminUser adminUser = adminUserRepository.findById(id).orElseThrow(()-> new AdminUserNotFoundException());
        adminUser.updateLastPasswordChangeDate();
    }
}