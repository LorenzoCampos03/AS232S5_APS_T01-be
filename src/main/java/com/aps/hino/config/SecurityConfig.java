package com.aps.hino.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import com.aps.hino.security.JwtAuthenticationFilter;
import com.aps.hino.security.SecurityContextRepository;

import java.util.Arrays;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final SecurityContextRepository securityContextRepository;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Permitir orígenes específicos
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",
                "http://localhost:3000",
                "http://127.0.0.1:5173"));

        // Permitir todos los métodos HTTP
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Permitir todos los headers
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // Exponer headers de autorización
        configuration.setExposedHeaders(Arrays.asList("Authorization"));

        // Permitir credenciales
        configuration.setAllowCredentials(true);

        // Tiempo de cache para preflight
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                // Enable CORS with our configuration
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Disable CSRF for REST API
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                // Disable form login
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)

                // Disable HTTP Basic
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)

                // Configure authorization
                .authorizeExchange(exchanges -> exchanges
                        // Allow OPTIONS requests (CORS preflight)
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Public endpoints (no authentication required)
                        .pathMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .pathMatchers("/api/public/**").permitAll()

                        // Swagger/OpenAPI endpoints
                        .pathMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/webjars/**")
                        .permitAll()

                        // All other /api/** endpoints require authentication
                        .pathMatchers("/api/**").authenticated()

                        // Allow all other requests
                        .anyExchange().permitAll())

                // Use custom security context repository
                .securityContextRepository(securityContextRepository)

                // Add JWT authentication filter
                .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)

                .build();
    }
}
