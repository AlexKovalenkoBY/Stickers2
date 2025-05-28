package com.example.uploadingfiles.config;

import java.time.Duration;

import com.example.uploadingfiles.services.WbProductsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;

@Configuration
public class BeanConfig {
   @Bean
   public RestTemplate restTemplate() {
      return new RestTemplateBuilder()
            .setConnectTimeout(Duration.ofSeconds(5))
            .setReadTimeout(Duration.ofSeconds(5))
            .build();
   }
       @Value("${wb.products.api.url:http://localhost:8080/api/wb/all-cards-mapped-short}")
    private String apiUrl;

    @Bean
    public WbProductsService wbProductsService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        return new WbProductsService(restTemplate, objectMapper, apiUrl);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
