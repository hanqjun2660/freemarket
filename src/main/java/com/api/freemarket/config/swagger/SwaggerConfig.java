package com.api.freemarket.config.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class SwaggerConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/")
                .setCachePeriod(0);
    }

    @Bean
    public OpenAPI openAPI() {

        Info info = new Info()
                .title("Freemarket API")
                .version("1.0")
                .description("Freemarket API 문서");

        // SecuritySecheme명
        String jwtSchemeName = "JwtAuth";
        // API 요청헤더에 인증정보 포함
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);
        // SecuritySchemes 등록
        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP) // HTTP 방식
                        .scheme("bearer")
                        .in(SecurityScheme.In.HEADER)
                        .bearerFormat("Authorization")); // 토큰 형식을 지정하는 임의의 문자(Optional)

        return new OpenAPI()
                .components(components)
                .addServersItem(new Server().url("/"))
                .addSecurityItem(securityRequirement)
                .info(info);
    }
}
