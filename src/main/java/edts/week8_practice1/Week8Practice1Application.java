package edts.week8_practice1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.ViewController;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class Week8Practice1Application implements WebMvcConfigurer {

    public static void main(String[] args) {
        SpringApplication.run(Week8Practice1Application.class, args);
    }

    /**
     * Redirect root path (/) to Swagger UI
     * When users open the domain, they'll be automatically redirected to API documentation
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Redirect root "/" to Swagger UI
        registry.addRedirectViewController("/", "/swagger-ui.html");

        // Also redirect common paths
        registry.addRedirectViewController("/index.html", "/swagger-ui.html");
        registry.addRedirectViewController("/api", "/swagger-ui.html");
    }

    /**
     * Fallback controller for any unmapped paths
     * Redirects to Swagger UI for better UX
     */
    @Bean
    public ViewController viewController() {
        return new ViewController() {
            @Override
            public String getViewName() {
                return "forward:/swagger-ui.html";
            }
        };
    }
}
