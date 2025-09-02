package br.com.jefersonmbs.recargapaywallet.domain.dto;

import lombok.Builder;
import lombok.Data;
import org.slf4j.MDC;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.UUID;


@Data
@Builder
public class AuditContext {
    
    private final String originIp;
    private final String userAgent;
    private final String sessionId;
    private final String correlationId;
    private final String createdBy;
    private final String metadata;
    

    public static AuditContext capture() {
        if (isContextAvailableInMDC()) {
            return captureFromMDC();
        }
        
        Optional<AuditContext> contextFromRequest = captureFromCurrentRequest();
        if (contextFromRequest.isPresent()) {
            return contextFromRequest.get();
        }
        
        return systemContext();
    }

    public static AuditContext withCorrelationId(String correlationId) {
        AuditContext baseContext = capture();
        
        return AuditContext.builder()
                .correlationId(correlationId != null ? correlationId : baseContext.getCorrelationId())
                .sessionId(baseContext.getSessionId())
                .originIp(baseContext.getOriginIp())
                .userAgent(baseContext.getUserAgent())
                .createdBy(baseContext.getCreatedBy())
                .metadata(baseContext.getMetadata())
                .build();
    }

    public static AuditContext manual(String correlationId, String createdBy) {
        return AuditContext.builder()
                .correlationId(correlationId)
                .createdBy(createdBy)
                .sessionId("MANUAL")
                .originIp("127.0.0.1")
                .userAgent("MANUAL")
                .build();
    }

    public static AuditContext systemContext() {
        return AuditContext.builder()
                .correlationId(generateCorrelationId())
                .createdBy("SYSTEM")
                .sessionId("SYSTEM")
                .originIp("127.0.0.1")
                .userAgent("SYSTEM_INTERNAL")
                .build();
    }

    private static boolean isContextAvailableInMDC() {
        return MDC.get("correlationId") != null;
    }

    private static AuditContext captureFromMDC() {
        return AuditContext.builder()
                .correlationId(MDC.get("correlationId"))
                .sessionId(MDC.get("sessionId"))
                .originIp(MDC.get("originIp"))
                .userAgent(MDC.get("userAgent"))
                .createdBy(MDC.get("createdBy") != null ? MDC.get("createdBy") : "SYSTEM")
                .metadata(MDC.get("metadata"))
                .build();
    }

    private static Optional<AuditContext> captureFromCurrentRequest() {
        try {
            ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            
            return Optional.of(AuditContext.builder()
                    .correlationId(getOrGenerateCorrelationId(request))
                    .sessionId(request.getSession().getId())
                    .originIp(getClientIp(request))
                    .userAgent(request.getHeader("User-Agent"))
                    .createdBy("EXTRACTED_FROM_REQUEST")
                    .build());
                    
        } catch (IllegalStateException e) {
            return Optional.empty();
        }
    }

    private static String getOrGenerateCorrelationId(HttpServletRequest request) {
        String existing = request.getHeader("X-Correlation-ID");
        return existing != null ? existing : generateCorrelationId();
    }

    private static String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    private static String generateCorrelationId() {
        return "CORR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}