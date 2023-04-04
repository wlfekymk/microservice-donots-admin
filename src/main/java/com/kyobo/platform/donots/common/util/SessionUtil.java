package com.kyobo.platform.donots.common.util;

import com.kyobo.platform.donots.common.exception.InvalidSessionException;
import com.kyobo.platform.donots.model.entity.AdminUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class SessionUtil {

    public static final int MAX_INACTIVE_INTERVAL_IN_SECONDS = 30/*분*/ * 60;
    public static final int MAX_INACTIVE_INTERVAL_IN_MINUTES = 30;

    public static HashMap<String, Object> validateAndGetSessionValueAndExtendSessionInterval(HttpSession httpSession) {
        log.info("SessionUtil.validateAndGetSessionValueAndExtendsSessionInterval Start");

        log.info("httpSession.getAttribute(\"sessionDto\")");
        Object sessionMapUncasted = httpSession.getAttribute("sessionDto");
        log.info("if (sessionMapUncasted == null || !(sessionMapUncasted instanceof Map))");
        if (sessionMapUncasted == null || !(sessionMapUncasted instanceof Map)) {
            log.info("throw new InvalidSessionException()");
            throw new InvalidSessionException();
        }

        log.info("httpSession.setMaxInactiveInterval(MAX_INACTIVE_INTERVAL)");
        // 세션유효시간 연장
        httpSession.setMaxInactiveInterval(MAX_INACTIVE_INTERVAL_IN_SECONDS);
        log.info("(HashMap<String, Object>) sessionMapUncasted");
        HashMap<String, Object> sessionMap = (HashMap<String, Object>) sessionMapUncasted;

        log.info("SessionUtil.validateAndGetSessionValueAndExtendSessionInterval End");
        return sessionMap;
    }

    public static HashMap<String, Object> validateAndGetGlobalCustomSessionValueAndExtendSessionInterval(HttpServletRequest request, RedisTemplate<String, Object> redisTemplate) {
        log.info("SessionUtil.validateAndGetGlobalCustomSessionValueAndExtendSessionInterval Start");

        Object sessionKeyUncasted = request.getHeader("sessionKey");
        if (sessionKeyUncasted == null || !(sessionKeyUncasted instanceof String)) {
            log.info("Header에 sessionKey 속성이 없음");
            throw new InvalidSessionException();
        }

        String sessionKey = (String) sessionKeyUncasted;

        ValueOperations<String, Object> operations = redisTemplate.opsForValue();
        Object sessionMapUncasted = operations.get(sessionKey);
        if (sessionMapUncasted == null || !(sessionMapUncasted instanceof Map)) {
            log.info("throw new InvalidSessionException()");
            throw new InvalidSessionException();
        }
        // TODO 여기서 로그인 페이지로 가이드 할 수 있도록 합의된 오류메시지를 전달해야함

        HashMap<String, Object> sessionMap = (HashMap<String, Object>) sessionMapUncasted;

        // 세션유효시간 연장
        redisTemplate.expire(sessionKey, MAX_INACTIVE_INTERVAL_IN_MINUTES, TimeUnit.MINUTES);

        log.info("SessionUtil.validateAndGetGlobalCustomSessionValueAndExtendSessionInterval End");
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
        httpSession.setMaxInactiveInterval(MAX_INACTIVE_INTERVAL_IN_SECONDS);

        log.info("SessionUtil.populateLocalSession End");
    }

    public static void populateGlobalCustomSession(RedisTemplate<String, Object> redisTemplate, AdminUser adminUser) {
        log.info("SessionUtil.populateGlobalCustomSession Start");

        Map<String, Object> sessionMap = new HashMap<>();
        String stringifiedAdminUserKey = String.valueOf(adminUser.getId());
        sessionMap.put("id", adminUser.getId());
        sessionMap.put("adminId", adminUser.getAdminId());

        ValueOperations<String, Object> operations = redisTemplate.opsForValue();
        operations.set(stringifiedAdminUserKey, sessionMap);
        redisTemplate.expire(stringifiedAdminUserKey, MAX_INACTIVE_INTERVAL_IN_MINUTES, TimeUnit.MINUTES);

        log.info("SessionUtil.populateGlobalCustomSession End");
    }

    private static void extendSessionInterval(HttpSession httpSession) {
        log.info("SessionUtil.extendSessionInterval Start");

        httpSession.setMaxInactiveInterval(MAX_INACTIVE_INTERVAL_IN_SECONDS);

        log.info("SessionUtil.extendSessionInterval End");
    }

    public static void extendGlobalCustomSessionInterval(RedisTemplate<String, Object> redisTemplate, String adminUserKey) {
        log.info("SessionUtil.extendGlobalCustomSessionInterval Start");

        redisTemplate.expire(adminUserKey, MAX_INACTIVE_INTERVAL_IN_MINUTES, TimeUnit.MINUTES);

        log.info("SessionUtil.extendGlobalCustomSessionInterval End");
    }

    public static void extendGlobalCustomSessionInterval(RedisTemplate<String, Object> redisTemplate, Long adminUserKey) {
        log.info("SessionUtil.extendGlobalCustomSessionInterval Start");

        String stringifiedAdminUserKey = String.valueOf(adminUserKey);
        extendGlobalCustomSessionInterval(redisTemplate, stringifiedAdminUserKey);

        log.info("SessionUtil.extendGlobalCustomSessionInterval End");
    }

    public static HashMap<String, Object> getGlobalCustomSessionValue(HttpServletRequest request, RedisTemplate<String, Object> redisTemplate) {
        log.info("SessionUtil.getGlobalCustomSessionValue Start");

        String sessionKey = request.getHeader("sessionKey");

        ValueOperations<String, Object> operations = redisTemplate.opsForValue();
        HashMap<String, Object> sessionMap = (HashMap<String, Object>) operations.get(sessionKey);

        log.info("SessionUtil.getGlobalCustomSessionValue End");
        return sessionMap;
    }

    public static Long getGlobalCustomSessionLongAttribute(String attributeName, HttpServletRequest request, RedisTemplate<String, Object> redisTemplate) {
        log.info("SessionUtil.getGlobalCustomSessionLongAttribute Start");

        String sessionKey = request.getHeader("sessionKey");

        ValueOperations<String, Object> operations = redisTemplate.opsForValue();
        HashMap<String, Object> sessionMap = (HashMap<String, Object>) operations.get(sessionKey);
        Long attributeValue = Long.parseLong(sessionMap.get(attributeName).toString());

        log.info("SessionUtil.getGlobalCustomSessionLongAttribute End");
        return attributeValue;
    }

    public static String getGlobalCustomSessionStringAttribute(String attributeName, HttpServletRequest request, RedisTemplate<String, Object> redisTemplate) {
        log.info("SessionUtil.getGlobalCustomSessionStringAttribute Start");

        String sessionKey = request.getHeader("sessionKey");

        ValueOperations<String, Object> operations = redisTemplate.opsForValue();
        HashMap<String, Object> sessionMap = (HashMap<String, Object>) operations.get(sessionKey);
        String attributeValue = sessionMap.get(attributeName).toString();

        log.info("SessionUtil.getGlobalCustomSessionStringAttribute End");
        return attributeValue;
    }
}
