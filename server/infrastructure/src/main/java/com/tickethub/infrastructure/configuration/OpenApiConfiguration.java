package com.tickethub.infrastructure.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class OpenApiConfiguration {
    @Bean
    OpenAPI ticketHubOpenAPI() {
        return new OpenAPI().info(new Info().title("TicketHub API")
                .description("API de gerenciamento de shows, seções e spots.")
                .version("v1"));
    }
}
