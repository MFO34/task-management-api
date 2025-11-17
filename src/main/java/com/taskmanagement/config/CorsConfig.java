package com.taskmanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Frontend URL'leri (development + production)
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",      // React development
            "http://localhost:5173",      // Vite development
            "http://localhost:4200",      // Angular development
            "https://yourdomain.com"      // Production (değiştir!)
        ));
        
        // İzin verilen HTTP metodları
        configuration.setAllowedMethods(Arrays.asList(
            "GET", 
            "POST", 
            "PUT", 
            "DELETE", 
            "OPTIONS", 
            "PATCH"
        ));
        
        // İzin verilen header'lar
        configuration.setAllowedHeaders(List.of("*"));
        
        // Credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);
        
        // Preflight request cache süresi (1 saat)
        configuration.setMaxAge(3600L);
        
        // Frontend'in erişebileceği response header'lar
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type"
        ));
        
        // Tüm endpoint'lere uygula
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}