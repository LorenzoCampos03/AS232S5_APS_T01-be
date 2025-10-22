package com.aps.hino.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {
    
    @Value("${server.port:8080}")
    private String serverPort;
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("HinoPE API")
                        .version("1.0.0")
                        .description("API REST para el sistema de gestión de Hino Perú. " +
                                "Incluye gestión de vehículos, usuarios, cotizaciones y notificaciones.")
                        .contact(new Contact()
                                .name("Hino Perú")
                                .email("contacto@hino.com.pe"))
                        .license(new License()
                                .name("Privado")
                                .url("https://hino.com.pe")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Servidor de Desarrollo")))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Ingresa el token JWT obtenido del endpoint /api/auth/login")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"));
    }
}
