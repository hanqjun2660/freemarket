package com.api.freemarket.config.swagger;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import lombok.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class swaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .title("Freemarket API")
                .version("1.0")
                .description("2024 toy project Freemarket API");
        return new OpenAPI()
                .components(new Components())
                .info(info);
    }
}