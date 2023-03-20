package com.kyobo.platform.donots.common.filter;


import com.kyobo.platform.donots.common.util.SessionUtil;
import com.kyobo.platform.donots.model.repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;


@Slf4j
@Component
@RequiredArgsConstructor
public class SessionFilter implements Filter {

    private static final String SIGN_IN_URI_FROM_ROOT = "/login/v1/signIn";

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        log.info("SessionFilter.doFilter Start");

        HttpServletRequest request = (HttpServletRequest) req;
        log.info("request.getRequestURI(): {}", request.getRequestURI());
        log.info("HttpServletRequest.getSession().getId(): {}"+ request.getSession().getId());

        if (request.getRequestURI().equals(SIGN_IN_URI_FROM_ROOT)) {
            log.info("SessionFilter.doFilter End");
            chain.doFilter(req, res);
        }
        else {
            // 세션 유효성 검사를 통해 로그인 여부 판단하여 비로그인일 경우 Exception 메시지를 응답한다
            SessionUtil.validateAndGetGlobalCustomSessionValueAndExtendSessionInterval(request, redisTemplate);
//        SessionUtil.populateLocalSessionAndGlobalCustomSession(request.getSession(), redisTemplate, adminUser);

            log.info("SessionFilter.doFilter End");
            chain.doFilter(req, res);
        }
    }
}
