package shinzo.cineffi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import shinzo.cineffi.domain.entity.movie.Actor;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
@EnableJpaAuditing
public class CineffiApplication {
	public static void main(String[] args) {
		SpringApplication.run(CineffiApplication.class, args);
	}

}