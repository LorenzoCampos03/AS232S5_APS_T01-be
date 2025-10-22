package com.aps.hino.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Cotizaciones Hino")
                        .version("1.0")
                        .description("Documentación de los endpoints de Quote"));
    }

    // Esto agrega la configuración de seguridad aquí mismo
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
            .csrf().disable()
            .authorizeExchange()
                .pathMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .anyExchange().permitAll() // TODO: Cambiar a .authenticated() cuando agregues JWT
            .and()
            .httpBasic().disable()
            .formLogin().disable()
            .securityContextRepository(NoOpServerSecurityContextRepository.getInstance());

        return http.build();
    }
}
