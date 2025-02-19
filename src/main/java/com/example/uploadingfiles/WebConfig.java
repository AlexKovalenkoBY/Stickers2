package com.example.uploadingfiles;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.resource.EncodedResourceResolver;
import org.springframework.web.servlet.resource.PathResourceResolver;

@Configuration
public class WebConfig extends WebMvcConfigurationSupport {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Обработка веб-ресурсов
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/")
                .resourceChain(true)
                .addResolver(new EncodedResourceResolver());

        // Обработка статических ресурсов
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new EncodedResourceResolver());

        // Обработка favicon
        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/static/favicon.ico")
                .resourceChain(true)
                .addResolver(new EncodedResourceResolver());

        // Обработка изображения eac.png
        registry.addResourceHandler("/static/eac.png")
                .addResourceLocations("classpath:/static/eac.png")
                .resourceChain(true)
                .addResolver(new EncodedResourceResolver());

        // Обработка PDF-файлов
        registry.addResourceHandler("/pdf/**")
                .addResourceLocations("classpath:/static/pdf/")
                .resourceChain(true)
                .addResolver(new EncodedResourceResolver());
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Настройка CORS
        registry.addMapping("/**")
                .allowedOrigins(
                        "http://localhost:8080", "http://localhost:8081",
                        "http://127.0.0.1:8080", "http://127.0.0.1:5173", "http://localhost:5173",
                        "http://127.0.0.1:5174", "http://localhost:5174", "http://127.0.0.1:5175",
                        "http://localhost:5175"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Перенаправление всех запросов на index.html
        registry.addViewController("/{path:[a-zA-Z0-9-]+}").setViewName("forward:/index.html");
        registry.addViewController("/{path1:[a-zA-Z0-9-]+}/{path2:[a-zA-Z0-9-]+}")
                .setViewName("forward:/index.html");
        registry.addViewController("/{path1:[a-zA-Z0-9-]+}/{path2:[a-zA-Z0-9-]+}/{path3:[a-zA-Z0-9-]+}")
                .setViewName("forward:/index.html");
    }
}