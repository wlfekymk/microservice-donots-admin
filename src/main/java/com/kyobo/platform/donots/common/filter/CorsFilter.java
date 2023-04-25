package com.kyobo.platform.donots.common.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

@Order(1)
@Component
@Slf4j
public class CorsFilter implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) res;
        HttpServletRequest request = (HttpServletRequest) req;

//        log.info("RemoteAddr: "+ request.getRemoteAddr());
//        log.info("RemoteHost: "+ request.getRemoteHost());
//        log.info("RemotePort: "+ request.getRemotePort());
//        log.info("RemoteUser: "+ request.getRemoteUser());
//        log.info("RequestURI: "+ request.getRequestURI());
//        log.info("RequestURL: "+ request.getRequestURL());
//        log.info("ServerName: "+ request.getServerName());
//        log.info("ServerPort: "+ request.getServerPort());

//        Enumeration<String> parameterNames = request.getParameterNames();
//        log.info("HttpServletRequest.parameters are as follows if any:");
//        while (parameterNames.hasMoreElements()) {
//            String currentElem = parameterNames.nextElement();
//            log.info(currentElem + ": " + request.getParameter(currentElem.toString()));
//        }
//
//        Enumeration<String> headerNames = request.getHeaderNames();
//        log.info("HttpServletRequest.headers are as follows if any:");
//        while (headerNames.hasMoreElements()) {
//            String currentElem = headerNames.nextElement();
//            log.info(currentElem + ": " + request.getHeader(currentElem.toString()));
//        }

        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods","*");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Authorization, timestamp, sessionKey");

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            chain.doFilter(req, res);
        }
    }
}

