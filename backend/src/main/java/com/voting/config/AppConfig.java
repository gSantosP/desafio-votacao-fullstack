package com.voting.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AppConfig {

    @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:5173}")
    private String allowedOrigins;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOriginPatterns(allowedOrigins.split(","))
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }

    @Bean
    public OpenAPI votingOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Cooperative Voting System API")
                        .description("""
                                REST API for managing cooperative assembly voting sessions.
                                
                                **Features:**
                                - Create and manage agendas (pautas)
                                - Open timed voting sessions
                                - Cast votes (SIM/NÃO) with CPF identification
                                - Real-time vote counting and results
                                - CPF validation facade (Bonus Task 1)
                                
                                **API Versioning Strategy:** URI path versioning (/api/v1/...).
                                This approach was chosen for visibility, easy routing at the 
                                infrastructure level, and broad client compatibility.
                                """)
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Voting System")
                                .email("dev@voting.com"))
                        .license(new License().name("MIT")));
    }
}
