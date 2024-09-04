package shinzo.cineffi.movie.repository;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import shinzo.cineffi.domain.entity.movie.ActorMovie;

import java.util.Collection;
import java.util.List;

public interface ActorMovieRepository extends JpaRepository<ActorMovie, Long> {
    @Query("SELECT am FROM ActorMovie am WHERE am.movie.id = :movieId ORDER BY am.orders ASC")
    List<ActorMovie> findByMovieId(Long movieId, Pageable pageable);

    boolean existsByMovieIdAndActorId(Long movieId, Long actorId);
}
