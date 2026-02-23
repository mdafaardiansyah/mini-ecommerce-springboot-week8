package edts.week8_practice1;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Application Context Test
 *
 * NOTE: This test is DISABLED for CI/CD because it loads the full Spring Boot context
 * with H2 database, which takes 6+ minutes. This is unnecessary for fast CI/CD pipeline.
 *
 * - Service layer unit tests already cover all business logic with Mockito
 * - IntegrationTest.java already tests full application context
 * - This test only verifies "context loads" which provides minimal value
 *
 * To enable temporarily:
 * - Remove @Disabled annotation
 * - Or run: mvn test -Dtest=Week8Practice1ApplicationTests
 */
@SpringBootTest
@ActiveProfiles("test")
@Disabled("Full context test disabled for fast CI/CD. Context already tested in IntegrationTest")
class Week8Practice1ApplicationTests {

    @Test
    void contextLoads() {
        // Test that the application context loads successfully
    }

}
