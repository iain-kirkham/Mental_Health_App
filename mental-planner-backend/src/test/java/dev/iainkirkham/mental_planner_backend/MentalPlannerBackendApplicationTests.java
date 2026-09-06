package dev.iainkirkham.mental_planner_backend;

import dev.iainkirkham.mental_planner_backend.config.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class MentalPlannerBackendApplicationTests {

	/**
	 * Also the regression test for EncryptedStringConverter's init-order invariant (see its
	 * class Javadoc): every entity using that converter is registered in this context, so a
	 * bean-creation cycle reintroduced there would fail this test.
	 */
	@Test
	void contextLoads() {
	}

}
