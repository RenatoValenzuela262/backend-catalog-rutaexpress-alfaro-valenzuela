package com.rutaexpress.catalog.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI rutaexpressCatalogOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("RutaExpress Catalog API")
                        .description("Microservicio de catálogo: administra los servicios de envío de RutaExpress y "
                                + "la capacidad de flota disponible para cada uno. Sin autenticación en esta iteración.")
                        .version("v1.0.0")
                        .contact(new Contact().name("RutaExpress Backend Team")));
    }
}