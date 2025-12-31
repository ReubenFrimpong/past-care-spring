package com.reuben.pastcare_spring.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

/**
 * Configuration to support Angular client-side routing.
 *
 * This ensures that all non-API routes are forwarded to index.html,
 * allowing Angular to handle routing for paths like /dashboard, /members, etc.
 *
 * When running frontend separately (development mode), this gracefully handles
 * the case where index.html doesn't exist in the backend's static folder.
 */
@Configuration
public class SpaRoutingConfig implements WebMvcConfigurer {

    private static final Resource INDEX_HTML = new ClassPathResource("/static/index.html");
    private static final boolean INDEX_HTML_EXISTS = INDEX_HTML.exists();

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // In development mode (frontend running separately), configure basic static resource serving
        // but don't try to serve Angular routes
        if (!INDEX_HTML_EXISTS) {
            // Only serve actual static files (JS, CSS, images) if they exist
            // Don't try to handle Angular routes - they'll get 404 which is expected in dev mode
            registry
                .addResourceHandler("/assets/**", "/favicon.ico", "/*.js", "/*.css", "/*.map")
                .addResourceLocations("classpath:/static/", "classpath:/public/");
            return;
        }

        // Production mode: Serve static resources and forward Angular routes to index.html
        registry
            .addResourceHandler("/**")
            .addResourceLocations("classpath:/static/", "classpath:/public/")
            .resourceChain(true)
            .addResolver(new PathResourceResolver() {
                @Override
                protected Resource getResource(String resourcePath, Resource location) throws IOException {
                    Resource requestedResource = location.createRelative(resourcePath);

                    // If the resource exists, serve it (e.g., CSS, JS, images)
                    if (requestedResource.exists() && requestedResource.isReadable()) {
                        return requestedResource;
                    }

                    // If it's an API request, return null (let Spring handle it)
                    if (resourcePath.startsWith("api/")) {
                        return null;
                    }

                    // For all other routes (Angular routes), return index.html
                    // This allows Angular to handle client-side routing
                    return INDEX_HTML;
                }
            });
    }
}
