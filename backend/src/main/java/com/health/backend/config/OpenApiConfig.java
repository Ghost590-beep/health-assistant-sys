package com.health.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI healthAssistAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Health Assistance System API")
                        .version("1.0")
                        .description("REST API for Health Assistance System"));
    }
}