package br.com.jefersonmbs.recargapaywallet.infrastructure.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Slf4j
@Component
public class AuditContextInterceptor implements HandlerInterceptor {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String FORWARDED_FOR_HEADER = "X-Forwarded-For";
    private static final String REAL_IP_HEADER = "X-Real-IP";
    private static final String USER_AGENT_HEADER = "User-Agent";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String correlationId = getOrGenerateCorrelationId(request);
        String sessionId = request.getSession().getId();
        String originIp = getClientIpAddress(request);
        String userAgent = request.getHeader(USER_AGENT_HEADER);
        String createdBy = getCurrentUser(request);

        MDC.put("correlationId", correlationId);
        MDC.put("sessionId", sessionId);
        MDC.put("originIp", originIp);
        MDC.put("userAgent", userAgent);
        MDC.put("createdBy", createdBy);
        
        response.setHeader(CORRELATION_ID_HEADER, correlationId);
        
        log.debug("Audit context captured automatically - CorrelationId: {}, IP: {}, User: {}", 
                 correlationId, originIp, createdBy);
        
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                              Object handler, Exception ex) {
        MDC.clear();
        
        if (log.isTraceEnabled()) {
            log.trace("MDC cleared after request completion for: {}", request.getRequestURI());
        }
    }


    private String getOrGenerateCorrelationId(HttpServletRequest request) {
        String existingCorrelationId = request.getHeader(CORRELATION_ID_HEADER);
        if (existingCorrelationId != null && !existingCorrelationId.trim().isEmpty()) {
            return existingCorrelationId;
        }
        
        return "CORR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }


    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader(FORWARDED_FOR_HEADER);
        if (xForwardedFor != null && !xForwardedFor.trim().isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader(REAL_IP_HEADER);
        if (xRealIp != null && !xRealIp.trim().isEmpty()) {
            return xRealIp.trim();
        }

        return request.getRemoteAddr();
    }

    private String getCurrentUser(HttpServletRequest request) {
        //Futuranmento pegar do auth ou de outra forma
        return "SYSTEM";
    }
}