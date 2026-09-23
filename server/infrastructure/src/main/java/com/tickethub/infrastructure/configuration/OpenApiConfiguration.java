package com.tickethub.infrastructure.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class OpenApiConfiguration {
    @Bean
    OpenAPI ticketHubOpenAPI() {
        final Contact contact =
                new Contact().name("Matheus José Ferreira de Aguiar").email("matheusjfa@outlook.com");

        final Info info =
                new Info().title("TicketHub API").description("""
                        Plataforma TicketHub para gerenciamento de shows, seções, spots, clientes (customers) e parceiros (partners).

                        Arquitetura limpa em três módulos Maven com Java 25 e Spring Boot 4: `domain` (entidades e gateways), \
                        `application` (casos de uso) e `infrastructure` (API HTTP, MongoDB, Kafka e Liquibase). \
                        Oferece criação, busca por ID, listagem paginada e exclusão, além de ações específicas como \
                        publicar/despublicar shows, seções e spots.""").version("v1").contact(contact);

        return new OpenAPI().info(info);
    }
}
