package com.bigproject.fic2toon.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/image/**") // /image/** 요청 처리
                .addResourceLocations("classpath:/static/image/"); // static/image 디렉토리 매핑
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:///C:/uploads/");
    }
}
