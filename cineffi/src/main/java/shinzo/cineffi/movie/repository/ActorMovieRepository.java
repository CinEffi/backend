package shinzo.cineffi.movie.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shinzo.cineffi.domain.entity.movie.ActorMovie;

import java.util.Collection;
import java.util.List;

public interface ActorMovieRepository extends JpaRepository<ActorMovie, Long> {
    List<ActorMovie> findByMovieId(Long movieId);
}
