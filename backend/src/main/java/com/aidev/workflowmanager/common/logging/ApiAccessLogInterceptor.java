package com.aidev.workflowmanager.common.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ApiAccessLogInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(ApiAccessLogInterceptor.class);
    private static final String START_TIME_ATTRIBUTE = ApiAccessLogInterceptor.class.getName() + ".START_TIME";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME_ATTRIBUTE, System.currentTimeMillis());
        log.info("[API] incoming method={} uri={} query={}", request.getMethod(), request.getRequestURI(), safeQuery(request));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        long durationMs = durationMs(request);
        if (ex == null) {
            log.info("[API] completed method={} uri={} status={} durationMs={}",
                    request.getMethod(), request.getRequestURI(), response.getStatus(), durationMs);
            return;
        }
        log.warn("[API] failed method={} uri={} status={} durationMs={} error={}",
                request.getMethod(), request.getRequestURI(), response.getStatus(), durationMs, ex.getMessage());
    }

    private String safeQuery(HttpServletRequest request) {
        return request.getQueryString() == null ? "" : request.getQueryString();
    }

    private long durationMs(HttpServletRequest request) {
        Object startTime = request.getAttribute(START_TIME_ATTRIBUTE);
        if (!(startTime instanceof Long)) {
            return -1L;
        }
        return System.currentTimeMillis() - (Long) startTime;
    }
}
