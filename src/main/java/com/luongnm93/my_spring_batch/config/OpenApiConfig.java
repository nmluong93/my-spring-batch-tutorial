package com.luongnm93.my_spring_batch.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("My Spring Batch API")
                        .description("REST API for triggering and monitoring Spring Batch jobs")
                        .version("0.0.1")
                        .contact(new Contact()
                                .name("Luong Nguyen")
                                .email("nguyenminhluong100693@gmail.com")))
                .tags(List.of(
                        new Tag()
                                .name("Employee Jobs")
                                .description("Endpoints for uploading employee data files and triggering the employee import batch job")
                ));
    }
}
