package com.kyobo.platform.donots.common.filter;


import com.kyobo.platform.donots.common.util.SessionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Order(2)
@Component
@RequiredArgsConstructor
@Slf4j
public class SessionFilter implements Filter {

    private static final String REQUEST_URI_SIGN_IN = "/login/v1/signIn";
    private static final String REQUEST_URI_SWAGGER_KEYWORD = "/swagger-";
    private static final String REQUEST_URI_SWAGGER_API_DOCS_KEYWORD = "/api-docs";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        log.info("SessionFilter.doFilter Start");

        HttpServletRequest request = (HttpServletRequest) req;
        log.info("request.getRequestURI(): {}", request.getRequestURI());
        log.info("HttpServletRequest.getSession().getId(): {}", request.getSession().getId());

        String requestURI = request.getRequestURI();
        // TODO 개발계도 외부노출되므로 막아야함
        if (requestURI.equals(REQUEST_URI_SIGN_IN) || requestURI.contains(REQUEST_URI_SWAGGER_KEYWORD) || requestURI.contains(REQUEST_URI_SWAGGER_API_DOCS_KEYWORD)) {
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
