package br.com.jefersonmbs.recargapaywallet.infrastructure.config;

import br.com.jefersonmbs.recargapaywallet.infrastructure.interceptor.AuditContextInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AuditContextInterceptor auditContextInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("Registering AuditContextInterceptor for automatic context capture");
        
        registry.addInterceptor(auditContextInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/health",
                        "/actuator/**",
                        "/swagger-ui/**",
                        "/api/v*/api-docs/**"
                );
        
        log.info("AuditContextInterceptor registered successfully for path patterns: /api/**");
    }
}