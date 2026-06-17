package com.example.carmarketanalyzer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class PhotoStorageConfig implements WebMvcConfigurer {

    @Value("${app.photo-storage-dir:data/photos}")
    private String photoStorageDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String storageLocation = Path.of(photoStorageDir).toAbsolutePath().normalize().toUri().toString();
        if (!storageLocation.endsWith("/")) {
            storageLocation += "/";
        }

        registry.addResourceHandler("/photos/**")
                .addResourceLocations(storageLocation);
    }
}
