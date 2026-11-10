package com.ecomm.gateway.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * Global Gateway Logging and Correlation Filter.
 * 
 * - Generates or propagates unique correlation ID (X-Correlation-Id) across microservices.
 * - Records request method, path, remote client IP, and execution time.
 * - Injects X-Correlation-Id and X-Gateway-Duration-Ms headers into HTTP response.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GatewayLoggingFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(GatewayLoggingFilter.class);
    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String DURATION_HEADER = "X-Gateway-Duration-Ms";
    private static final String MDC_CORRELATION_KEY = "correlationId";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        if (!(servletRequest instanceof HttpServletRequest httpRequest) ||
            !(servletResponse instanceof HttpServletResponse httpResponse)) {
            chain.doFilter(servletRequest, servletResponse);
            return;
        }

        String path = httpRequest.getRequestURI();

        // Skip verbose logging for static Eureka dashboard assets
        if (isStaticAsset(path)) {
            chain.doFilter(servletRequest, servletResponse);
            return;
        }

        long startTime = System.currentTimeMillis();

        String correlationId = httpRequest.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        MDC.put(MDC_CORRELATION_KEY, correlationId);
        httpResponse.setHeader(CORRELATION_ID_HEADER, correlationId);

        String method = httpRequest.getMethod();
        String queryString = httpRequest.getQueryString();
        String fullPath = (queryString != null) ? path + "?" + queryString : path;

        log.info("[GATEWAY-IN]  {} {} from IP={} [correlationId={}]",
                method, fullPath, httpRequest.getRemoteAddr(), correlationId);

        try {
            chain.doFilter(servletRequest, servletResponse);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            httpResponse.setHeader(DURATION_HEADER, String.valueOf(duration));

            log.info("[GATEWAY-OUT] {} {} -> Status={} (took {}ms) [correlationId={}]",
                    method, fullPath, httpResponse.getStatus(), duration, correlationId);

            MDC.remove(MDC_CORRELATION_KEY);
        }
    }

    private boolean isStaticAsset(String path) {
        return path.startsWith("/eureka/css/") ||
               path.startsWith("/eureka/js/") ||
               path.startsWith("/eureka/fonts/") ||
               path.startsWith("/eureka/images/") ||
               path.equals("/favicon.ico");
    }
}
