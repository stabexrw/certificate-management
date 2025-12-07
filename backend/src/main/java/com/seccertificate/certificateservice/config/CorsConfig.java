package com.seccertificate.certificateservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS configuration is now handled in SecurityConfig.java
 * This class is kept for reference but is no longer used.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        // CORS is configured in SecurityConfig.corsConfigurationSource() instead
    }
}