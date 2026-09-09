package com.pulsesg.platform.core.app.store.configuration;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*"); // Allow all origins
        config.setExposedHeaders(List.of(
                "Content-Type", "X-PULSE-LOG-USER-ID", "X-PULSE-ORG-ID", "X-PULSE-TENANT-ID"
        ));
        config.setAllowedHeaders(List.of(
                "Origin", "Content-Type", "Accept", "Authorization", "updatedBy",
                "X-PULSE-LOG-USER-ID", "X-PULSE-ORG-ID", "X-PULSE-TENANT-ID", "X-PULSE-ROLES", "x-lang-id"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
