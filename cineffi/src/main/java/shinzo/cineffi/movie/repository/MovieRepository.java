package shinzo.cineffi.movie.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shinzo.cineffi.domain.entity.movie.Movie;

public interface MovieRepository extends JpaRepository<Movie, Long> {
}
