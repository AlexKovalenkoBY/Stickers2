package com.example.uploadingfiles;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.resource.EncodedResourceResolver;
// import org.springframework.web.servlet.resource.GzipResourceResolver;

import org.springframework.web.servlet.resource.PathResourceResolver;

@Configuration

public class WebConfig extends WebMvcConfigurationSupport {

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/").resourceChain(true)
    .addResolver(new EncodedResourceResolver());

    registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/").resourceChain(true)
                .addResolver(new EncodedResourceResolver());
    registry.addResourceHandler("/favicon.ico").addResourceLocations("classpath:/static/favicon.ico").resourceChain(true)
    .addResolver(new EncodedResourceResolver());
    // registry.addResourceHandler("/static/eac.png").addResourceLocations("classpath:/static/eac.png");
    registry.addResourceHandler("/static/eac.png").addResourceLocations(" classpath:/static/eac.png").resourceChain(true)
    .addResolver(new EncodedResourceResolver());
    registry.addResourceHandler("/pdf/**").addResourceLocations("classpath:/static/pdf/").resourceChain(true)
    .addResolver(new EncodedResourceResolver());
    /*
     * The following curl request will display the Hello.html page stored in either
     * the application’s webappp/resources or the other-resources folder in the
     * classpath:
     */
    // registry
    //     .addResourceHandler("/resources/**")
    //     .addResourceLocations("/resources/", "classpath:/resources/").setCachePeriod(0)
    //     .resourceChain(true)
    //     .addResolver(new PathResourceResolver());
    registry
        .addResourceHandler("/static/eac.png")
        .addResourceLocations("/static/eac.png", "classpath:/resources/static/eac.png").setCachePeriod(0)
        .resourceChain(true)
        .addResolver(new PathResourceResolver());

  }

}