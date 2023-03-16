package com.kyobo.platform.donots.common.util;

import com.kyobo.platform.donots.common.exception.BusinessException;
import com.kyobo.platform.donots.model.entity.AdminUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class SessionUtil {

    public static final int MAX_INACTIVE_INTERVAL = 30/*분*/ * 60;

    public static HashMap<String, Object> validateAndGetSessionValueAndExtendSessionInterval(HttpSession httpSession) {
        log.info("SessionUtil.validateAndGetSessionValueAndExtendsSessionInterval Start");

        Object sessionMapUncasted = httpSession.getAttribute("sessionDto");
        if (sessionMapUncasted == null || !(sessionMapUncasted instanceof Map))
            throw new BusinessException("Session 정보가 없거나 유효하지 않습니다.");

        // 세션유효시간 연장
        httpSession.setMaxInactiveInterval(MAX_INACTIVE_INTERVAL);
        HashMap<String, Object> sessionMap = (HashMap<String, Object>) sessionMapUncasted;

        log.info("SessionUtil.validateAndGetSessionValue End");
        return sessionMap;
    }

    public static void populateLocalSessionAndGlobalCustomSession(HttpSession httpSession, RedisTemplate<String, Object> redisTemplate, AdminUser adminUser) {
        log.info("SessionUtil.populateLocalSessionAndGlobalCustomSession Start");

        populateLocalSession(httpSession, adminUser);
        populateGlobalCustomSession(redisTemplate, adminUser);

        log.info("SessionUtil.populateLocalSessionAndGlobalCustomSession End");
    }

    private static void populateLocalSession(HttpSession httpSession, AdminUser adminUser) {
        log.info("SessionUtil.populateLocalSession Start");

        Map<String, Object> sessionMap = new HashMap<>();
        sessionMap.put("id", adminUser.getId());
        sessionMap.put("adminId", adminUser.getAdminId());

        httpSession.setAttribute("sessionDto", sessionMap);
        httpSession.setMaxInactiveInterval(MAX_INACTIVE_INTERVAL);

        log.info("SessionUtil.populateLocalSession End");
    }

    private static void populateGlobalCustomSession(RedisTemplate<String, Object> redisTemplate, AdminUser adminUser) {
        log.info("SessionUtil.populateGlobalCustomSession Start");

        Map<String, Object> sessionMap = new HashMap<>();
        String stringifiedAdminUserKey = String.valueOf(adminUser.getId());
        sessionMap.put("id", adminUser.getId());
        sessionMap.put("adminId", adminUser.getAdminId());

        ValueOperations<String, Object> operations = redisTemplate.opsForValue();
        operations.set(stringifiedAdminUserKey, sessionMap);
        redisTemplate.expire(stringifiedAdminUserKey, MAX_INACTIVE_INTERVAL, TimeUnit.MINUTES);

        log.info("SessionUtil.populateGlobalCustomSession End");
    }

    private static void extendSessionInterval(HttpSession httpSession) {
        log.info("SessionUtil.extendSessionInterval Start");

        httpSession.setMaxInactiveInterval(MAX_INACTIVE_INTERVAL);

        log.info("SessionUtil.extendSessionInterval End");
    }

    public static void extendGlobalCustomSessionInterval(RedisTemplate<String, Object> redisTemplate, Long adminUserKey) {
        log.info("SessionUtil.extendGlobalCustomSessionInterval Start");

        String stringifiedAdminUserKey = String.valueOf(adminUserKey);
        redisTemplate.expire(stringifiedAdminUserKey, MAX_INACTIVE_INTERVAL, TimeUnit.MINUTES);

        log.info("SessionUtil.extendGlobalCustomSessionInterval End");
    }

    public static void extendGlobalCustomSessionInterval(RedisTemplate<String, Object> redisTemplate, String adminUserKey) {
        log.info("SessionUtil.extendGlobalCustomSessionInterval Start");

        redisTemplate.expire(adminUserKey, MAX_INACTIVE_INTERVAL, TimeUnit.MINUTES);

        log.info("SessionUtil.extendGlobalCustomSessionInterval End");
    }
}
