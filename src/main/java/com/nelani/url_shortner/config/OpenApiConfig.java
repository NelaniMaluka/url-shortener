package com.nelani.url_shortner.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

        @Bean
        public OpenAPI customOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("URL Shortener API")
                                                .version("v1")
                                                .description("API documentation for the URL Shortener Backend Service")
                                                .contact(new Contact()
                                                                .name("Nelani Maluka")
                                                                .email("malukanelani@gmail.com"))
                                                .license(new License()
                                                                .name("MIT")
                                                                .url("https://opensource.org/licenses/MIT")))
                                .servers(List.of(
                                                new Server()
                                                                .url("http://localhost:8080")
                                                                .description("Local Development Server"),
                                                new Server()
                                                                .url("https://url-shortener-4yxt.onrender.com")
                                                                .description("Production Server")))
                                .components(new Components());
        }

}
