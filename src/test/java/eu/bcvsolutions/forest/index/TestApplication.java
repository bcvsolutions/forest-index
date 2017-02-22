package eu.bcvsolutions.forest.index;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Test app
 * 
 * @author Radek Tomiška
 */
@EnableJpaRepositories
@SpringBootApplication
public class TestApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(TestApplication.class, args);
	}
}
