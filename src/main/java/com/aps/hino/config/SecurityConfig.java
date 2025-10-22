package com.aps.hino.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable) // 🚫 Desactiva CSRF
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/api/**").permitAll() // ✅ Deja libre todo lo de /api/
                        .anyExchange().permitAll() // ✅ Permite cualquier otra ruta
                )
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable) // ❌ Quita login básico
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable) // ❌ Quita formulario de login
                .build();
    }
}
