package shinzo.cineffi.movie.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shinzo.cineffi.domain.entity.movie.MovieGenre;

public interface MovieGenreRepository extends JpaRepository<MovieGenre, Long> {
}
