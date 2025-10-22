package com.aps.hino.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        // Puerto que Codespaces asigna públicamente
        String codespacePort = System.getenv("PORT"); // si es null, usar 8080 local
        String codespaceName = System.getenv("CODESPACE_NAME");

        Server server;

        if (codespaceName != null) {
            String port = (codespacePort != null) ? codespacePort : "8080";
            server = new Server()
                    .url("https://" + codespaceName + "-" + port + ".app.github.dev")
                    .description("Codespaces Server");
        } else {
            server = new Server()
                    .url("http://localhost:8080")
                    .description("Local Server");
        }

        return new OpenAPI()
                .info(new Info()
                        .title("Hino API")
                        .version("1.0")
                        .description("API para gestión de cotizaciones de transporte"))
                .servers(List.of(server));
    }
}
