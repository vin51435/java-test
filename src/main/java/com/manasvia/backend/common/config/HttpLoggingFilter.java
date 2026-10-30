package com.manasvia.backend.common.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Enterprise HTTP Access & Request Logging Filter (Morgan equivalent in Spring Boot).
 *
 * Capabilities:
 * 1. Attaches a unique X-Request-Id (MDC) to trace logs across services and virtual threads.
 * 2. Measures HTTP latency in milliseconds.
 * 3. Logs incoming methods, URIs, response status codes, and client IPs.
 * 4. Injects X-Request-Id into HTTP response headers for Android client-side correlation.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class HttpLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(HttpLoggingFilter.class);

    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    public static final String MDC_REQUEST_ID_KEY = "requestId";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        long startTime = System.currentTimeMillis();

        // 1. Resolve or generate unique Request ID
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString().substring(0, 8);
        }

        // 2. Put into MDC so all service/repo logs automatically inherit this ID
        MDC.put(MDC_REQUEST_ID_KEY, requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);

        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString() != null ? "?" + request.getQueryString() : "";
        String clientIp = extractClientIp(request);

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            int status = response.getStatus();

            // 3. Format Morgan-style console log with appropriate severity
            String logMessage = String.format(
                    "[HTTP] %s %s%s -> %d in %dms (client: %s)",
                    method, uri, queryString, status, duration, clientIp
            );

            if (status >= 500) {
                log.error(logMessage);
            } else if (status >= 400) {
                log.warn(logMessage);
            } else {
                log.info(logMessage);
            }

            // 4. Clear MDC to prevent virtual thread memory / context leaks
            MDC.remove(MDC_REQUEST_ID_KEY);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Skip noisy static assets for Swagger UI
        return path.startsWith("/swagger-ui/") && (
                path.endsWith(".css") || path.endsWith(".js") || path.endsWith(".png") || path.endsWith(".ico")
        );
    }

    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
