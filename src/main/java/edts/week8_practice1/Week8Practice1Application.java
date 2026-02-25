package edts.week8_practice1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@SpringBootApplication
public class Week8Practice1Application {

    public static void main(String[] args) {
        SpringApplication.run(Week8Practice1Application.class, args);
    }

    /**
     * Redirect controller for root path to Swagger UI
     * When users open the domain, they'll be automatically redirected to API documentation
     */
    @Controller
    public static class RedirectController {

        @GetMapping({"/", "/index.html", "/api"})
        public String redirectToSwagger() {
            return "redirect:/swagger-ui.html";
        }
    }
}
