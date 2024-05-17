package shinzo.cineffi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import shinzo.cineffi.domain.entity.movie.Actor;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class CineffiApplication {

	public static void main(String[] args) {
		SpringApplication.run(CineffiApplication.class, args);
	}
}
