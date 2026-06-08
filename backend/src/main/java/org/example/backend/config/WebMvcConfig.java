package org.example.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload.base-dir}")
    private String uploadBaseDir;

    @Value("${app.upload.access-url-prefix:/uploads/}")
    private String uploadAccessUrlPrefix;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String accessPrefix = uploadAccessUrlPrefix.endsWith("/")
                ? uploadAccessUrlPrefix + "**"
                : uploadAccessUrlPrefix + "/**";

        String location = Paths.get(uploadBaseDir).toUri().toString();
        registry.addResourceHandler(accessPrefix)
                .addResourceLocations(location);
    }
}