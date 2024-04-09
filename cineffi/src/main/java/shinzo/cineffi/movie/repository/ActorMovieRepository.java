package shinzo.cineffi.movie.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shinzo.cineffi.domain.entity.movie.ActorMovie;

public interface ActorMovieRepository extends JpaRepository<ActorMovie, Long> {
}
