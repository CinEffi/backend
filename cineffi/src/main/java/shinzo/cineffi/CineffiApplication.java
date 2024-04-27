package shinzo.cineffi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import shinzo.cineffi.domain.entity.movie.Actor;

// @SpringBootApplication(exclude = SecurityAutoConfiguration.class)
@SpringBootApplication
@EnableJpaAuditing
public class CineffiApplication {
	public static void main(String[] args) { SpringApplication.run(CineffiApplication.class, args); }
	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**").allowedOrigins("*");
			}
		};
	}


}