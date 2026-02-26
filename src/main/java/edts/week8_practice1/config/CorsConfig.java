package edts.week8_practice1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.Collections;

/**
 * CORS Configuration for Cross-Origin Resource Sharing
 *
 * This configuration allows the frontend (https://idp-week8.glanze.space)
 * to make API requests to the backend (http://idp-week8.glanze.space)
 *
 * IMPORTANT: This is a temporary solution for development/testing.
 * For production, consider:
 * 1. Using HTTPS for both frontend and backend (Heroku provides this)
 * 2. Using a reverse proxy (Nginx) to handle CORS
 * 3. Restricting allowed origins to specific domains only
 */
@Configuration
public class CorsConfig {

    /**
     * CORS Configuration Source
     * Allows specific origins, methods, and headers
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow specific frontend origins
        configuration.setAllowedOrigins(Arrays.asList(
            "https://idp-week8.glanze.space",
            "https://www.idp-week8.glanze.space"
        ));

        // For local development
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",
            "http://localhost:3001",
            "http://127.0.0.1:3000"
        ));

        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        // Allowed HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
            "GET",
            "POST",
            "PUT",
            "DELETE",
            "PATCH",
            "OPTIONS"
        ));

        // Allowed headers
        configuration.setAllowedHeaders(Arrays.asList(
            "Content-Type",
            "Authorization",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));

        // Exposed headers (what the browser can read from response)
        configuration.setExposedHeaders(Arrays.asList(
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials",
            "X-Total-Count",
            "X-Page-Count"
        ));

        // How long preflight request cache is valid (1 hour)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /**
     * CORS Filter
     * Processes CORS preflight requests (OPTIONS)
     */
    @Bean
    public CorsFilter corsFilter() {
        return new CorsFilter(corsConfigurationSource());
    }
}
