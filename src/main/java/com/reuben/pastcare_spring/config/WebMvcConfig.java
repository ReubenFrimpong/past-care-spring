package com.reuben.pastcare_spring.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Web MVC Configuration for the PastCare application.
 *
 * <p>Configures:
 * <ul>
 *   <li>Static resource handling for uploaded files</li>
 *   <li>Hibernate filter interceptor for automatic tenant isolation</li>
 * </ul>
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:uploads/profile-images}")
    private String uploadDir;

    private final HibernateFilterInterceptor hibernateFilterInterceptor;

    /**
     * Register static resource handlers for serving uploaded files.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadPath = Paths.get(uploadDir).toAbsolutePath().toUri().toString();

        registry.addResourceHandler("/api/uploads/**")
                .addResourceLocations(uploadPath.replace("/profile-images", "/"));
    }

    /**
     * Register interceptors for request processing.
     *
     * <p>The HibernateFilterInterceptor enables automatic tenant-scoped filtering
     * for all database queries, ensuring data isolation between churches.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(hibernateFilterInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/**", "/api/public/**");
    }
}
