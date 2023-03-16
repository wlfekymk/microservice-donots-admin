package com.kyobo.platform.donots.common.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@Aspect
public class LoggerAspect {

    @Pointcut("execution(* com.kyobo.platform.donots.controller.*Controller.*(..))")
    public void logPointCut() {
    }

    @Around("logPointCut()")
    public Object methodlog(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

        try {
            Object result = proceedingJoinPoint.proceed();

            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

            String controllerName = proceedingJoinPoint.getSignature().getDeclaringType().getSimpleName();
            String methodName = proceedingJoinPoint.getSignature().getName();

            Map<String, Object> params = new HashMap<>();

            try {
                params.put("controller", controllerName);
                params.put("method", methodName);
//                params.put("params", getParams(request));
                params.put("log_time", new Date());
                params.put("request_uri", request.getRequestURI());
                params.put("http_method", request.getMethod());
            } catch (Exception e) {
                log.error("logAspect error", e);
            }
            log.info("params : {}", params);

            return result;

        } catch (Throwable throwable) {
            throw throwable;
        }
    }


}