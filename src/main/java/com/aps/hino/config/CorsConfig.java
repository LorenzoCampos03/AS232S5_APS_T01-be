package com.aps.hino.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * Configuración global de CORS para permitir peticiones desde el frontend.
 * Compatible con WebFlux (Spring Boot Reactivo).
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        
        // 🌍 Permitir todos los orígenes (para desarrollo)
        corsConfig.addAllowedOriginPattern("*");
        
        // 📤 Métodos HTTP permitidos
        corsConfig.addAllowedMethod("*");
        
        // 📦 Encabezados permitidos
        corsConfig.addAllowedHeader("*");
        
        // 🔁 Permitir envío de credenciales (cookies o auth headers)
        corsConfig.setAllowCredentials(true);

        // Asignar la configuración a todas las rutas
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}