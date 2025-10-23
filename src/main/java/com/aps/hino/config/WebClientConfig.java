package com.aps.hino.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient() {
        // Puedes configurar aquí una URL base o headers globales si lo deseas
        return WebClient.builder().build();
    }
}
